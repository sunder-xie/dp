package dp.common.util.excelutil;

import dp.common.util.Print;
import org.apache.poi.hssf.eventusermodel.*;
import org.apache.poi.hssf.eventusermodel.dummyrecord.LastCellOfRowDummyRecord;
import org.apache.poi.hssf.eventusermodel.dummyrecord.MissingCellDummyRecord;
import org.apache.poi.hssf.model.HSSFFormulaParser;
import org.apache.poi.hssf.record.*;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangzhangting on 15/7/5.
 */
//读取Excel .xls
public abstract class ReadExcelXLS implements HSSFListener {
    private int minColumns;
    private POIFSFileSystem fs;

    private int lastRowNumber;
    private int lastColumnNumber;

    /** Should we output the formula, or the value it has? */
    private boolean outputFormulaValues = true;

    /** For parsing Formulas */
    private EventWorkbookBuilder.SheetRecordCollectingListener workbookBuildingListener;
    private HSSFWorkbook stubWorkbook;

    // Records we pick up as we process
    private SSTRecord sstRecord;
    private FormatTrackingHSSFListener formatListener;

    /** So we known which sheet we're on */
    private int sheetIndex = -1;
    private BoundSheetRecord[] orderedBSRs;
    private List<BoundSheetRecord> boundSheetRecords = new ArrayList<BoundSheetRecord>();

    // For handling formulas with string results
    private int nextRow;
    private int nextColumn;
    private boolean outputNextStringRecord;

    private List<String> rowList;

    //只处理一个sheet时使用，指定的待处理sheet，默认处理第一个
    protected int needHandleSheet = -1;


    /**
     *  minColumns: 列数
     */
    public void process(String filename, int minColumns) throws IOException {
        Print.info(filename);

        this.fs = new POIFSFileSystem(new FileInputStream(filename));
        this.minColumns = minColumns-1;
        this.rowList = new ArrayList<>();

        MissingRecordAwareHSSFListener listener = new MissingRecordAwareHSSFListener(this);
        formatListener = new FormatTrackingHSSFListener(listener);

        HSSFEventFactory factory = new HSSFEventFactory();
        HSSFRequest request = new HSSFRequest();

        if(outputFormulaValues) {
            request.addListenerForAllRecords(formatListener);
        } else {
            workbookBuildingListener = new EventWorkbookBuilder.SheetRecordCollectingListener(formatListener);
            request.addListenerForAllRecords(workbookBuildingListener);
        }

        factory.processWorkbookEvents(request, fs);
    }

    /**
     * 指定需要读取的sheet，从 1 开始
     * */
    public void processOneSheet(String filename, int minColumns, int sheetIndex) throws IOException {
        if(sheetIndex<1){
            needHandleSheet = 0;
        }else{
            needHandleSheet = sheetIndex - 1;
        }

        this.process(filename, minColumns);
    }
    public void processFirstSheet(String filename, int minColumns) throws IOException {
        this.processOneSheet(filename, minColumns, 1);
    }


    /**
     * Main HSSFListener method, processes events, and outputs the
     *  CSV as the file is processed.
     */
    public void processRecord(Record record) {

        int thisRow = -1;
        int thisColumn = -1;
        String thisStr = null;

        switch (record.getSid())
        {
            case BoundSheetRecord.sid:
                boundSheetRecords.add((BoundSheetRecord)record);
                break;
            case BOFRecord.sid:
                BOFRecord br = (BOFRecord)record;
                if(br.getType() == BOFRecord.TYPE_WORKSHEET) {
                    // Create sub workbook if required
                    if(workbookBuildingListener != null && stubWorkbook == null) {
                        stubWorkbook = workbookBuildingListener.getStubHSSFWorkbook();
                    }

                    // Output the worksheet name
                    // Works by ordering the BSRs by the location of
                    //  their BOFRecords, and then knowing that we
                    //  process BOFRecords in byte offset order
                    sheetIndex++;
                    if(orderedBSRs == null) {
                        orderedBSRs = BoundSheetRecord.orderByBofPosition(boundSheetRecords);
                    }
//                    System.out.println(
//                            orderedBSRs[sheetIndex].getSheetname() +
//                                    " [" + (sheetIndex + 1) + "]:"
//                    );
                }
                break;

            case SSTRecord.sid:
                sstRecord = (SSTRecord) record;
                break;

            case BlankRecord.sid:
                BlankRecord brec = (BlankRecord) record;

                thisRow = brec.getRow();
                thisColumn = brec.getColumn();
                thisStr = "";
                break;
            case BoolErrRecord.sid:
                BoolErrRecord berec = (BoolErrRecord) record;

                thisRow = berec.getRow();
                thisColumn = berec.getColumn();
                thisStr = "";
                break;

            case FormulaRecord.sid:
                FormulaRecord frec = (FormulaRecord) record;

                thisRow = frec.getRow();
                thisColumn = frec.getColumn();

                if(outputFormulaValues) {
                    if(Double.isNaN(frec.getValue())) {
                        // Formula result is a string
                        // This is stored in the next record
                        outputNextStringRecord = true;
                        nextRow = frec.getRow();
                        nextColumn = frec.getColumn();
                    } else {
                        thisStr = formatListener.formatNumberDateCell(frec);
                    }
                } else {
                    thisStr = HSSFFormulaParser.toFormulaString(stubWorkbook, frec.getParsedExpression());
                }
                break;
            case StringRecord.sid:
                if(outputNextStringRecord) {
                    // String for formula
                    StringRecord srec = (StringRecord)record;
                    thisStr = srec.getString();
                    thisRow = nextRow;
                    thisColumn = nextColumn;
                    outputNextStringRecord = false;
                }
                break;

            case LabelRecord.sid:
                LabelRecord lrec = (LabelRecord) record;

                thisRow = lrec.getRow();
                thisColumn = lrec.getColumn();
                thisStr = lrec.getValue();
                break;
            case LabelSSTRecord.sid:
                LabelSSTRecord lsrec = (LabelSSTRecord) record;

                thisRow = lsrec.getRow();
                thisColumn = lsrec.getColumn();
                if(sstRecord == null) {
                    thisStr = "";
                } else {
                    thisStr = sstRecord.getString(lsrec.getSSTIndex()).toString();
                }
                break;
            case NoteRecord.sid:
                NoteRecord nrec = (NoteRecord) record;

                thisRow = nrec.getRow();
                thisColumn = nrec.getColumn();
                thisStr = "";
                break;
            case NumberRecord.sid:
                NumberRecord numrec = (NumberRecord) record;

                thisRow = numrec.getRow();
                thisColumn = numrec.getColumn();

                // Format
                thisStr = formatListener.formatNumberDateCell(numrec);
                break;
            case RKRecord.sid:
                RKRecord rkrec = (RKRecord) record;

                thisRow = rkrec.getRow();
                thisColumn = rkrec.getColumn();
                thisStr = "";
                break;
            default:
                break;
        }

        // Handle new row
        if(thisRow != -1 && thisRow != lastRowNumber) {
            lastColumnNumber = -1;
        }

        // Handle missing column
        if(record instanceof MissingCellDummyRecord) {
            MissingCellDummyRecord mc = (MissingCellDummyRecord)record;
            thisRow = mc.getRow();
            thisColumn = mc.getColumn();
            thisStr = "";
        }

        // If we got something to print out, do so
        if(thisStr != null) {
            rowList.add(thisStr.trim());
        }

        // Update column and row count
        if(thisRow > -1)
            lastRowNumber = thisRow;
        if(thisColumn > -1)
            lastColumnNumber = thisColumn;

        // Handle end of row
        if(record instanceof LastCellOfRowDummyRecord) {
            // Print out any missing commas if needed
            //System.out.println(lastColumnNumber+"  "+minColumns);
            if(minColumns > 0) {
                // Columns are 0 based
                if(lastColumnNumber == -1) { lastColumnNumber = 0; }
                for(int i=lastColumnNumber; i<(minColumns); i++) {
                    rowList.add("");
                }
            }

            // End the row
            try {
                operateRows(sheetIndex, lastRowNumber, rowList);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // We're onto a new row
            lastColumnNumber = -1;
            rowList.clear();
        }
    }


    //excel记录行操作方法
    public abstract void operateRows(int sheetIndex, int curRow, List<String> rowList) throws Exception;

}
