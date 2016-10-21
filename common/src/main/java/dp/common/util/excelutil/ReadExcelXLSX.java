package dp.common.util.excelutil;

import dp.common.util.Print;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by huangzhangting on 15/7/5.
 */

//读取Excel .xlsx
public abstract class ReadExcelXLSX {

    enum xssfDataType {
        BOOL,
        ERROR,
        FORMULA,
        INLINESTR,
        SSTINDEX,
        NUMBER,
    }

    private int sheetIndex = -1;
    private int curRow = 0;		//当前行
    private int titleRow = 0;	//标题行，一般情况下为0

    //excel记录行操作方法，以sheet索引，行索引和行元素列表为参数，对sheet的一行元素进行操作，元素为String类型
    public abstract void operateRows(int sheetIndex, int curRow, List<String> rowList) throws Exception;

    //只遍历一个sheet，其中sheetId为要遍历的sheet索引，从1开始
    /**
     *
     * @param fileName
     * @param sheetId  sheetId为要遍历的sheet索引，从1开始
     * @throws Exception
     */
    public void processOneSheet(String fileName, int sheetId) throws Exception {
        Print.info(fileName);

        if(sheetId<1){
            sheetId = 1;
        }

        OPCPackage pkg = OPCPackage.open(fileName);
        XSSFReader r = new XSSFReader(pkg);
        SharedStringsTable sst = r.getSharedStringsTable();
        StylesTable styles = r.getStylesTable();
        XMLReader parser = fetchSheetParser(styles, sst);

        // 根据 rId# 或 rSheet# 查找sheet
        InputStream sheet = r.getSheet("rId"+sheetId);
        sheetIndex++;
        InputSource sheetSource = new InputSource(sheet);
        parser.parse(sheetSource);
        sheet.close();
    }

    public void processFirstSheet(String fileName) throws Exception{
        processOneSheet(fileName, 1);
    }

    /**
     * 遍历 excel 文件
     */
    public void processAllSheets(String fileName) throws Exception {
        Print.info(fileName);

        OPCPackage pkg = OPCPackage.open(fileName);
        XSSFReader r = new XSSFReader(pkg);
        SharedStringsTable sst = r.getSharedStringsTable();
        StylesTable styles = r.getStylesTable();
        XMLReader parser = fetchSheetParser(styles, sst);

        Iterator<InputStream> sheets = r.getSheetsData();
        while (sheets.hasNext()) {
            sheetIndex++;
            curRow = 0;
            InputStream sheet = sheets.next();
            InputSource sheetSource = new InputSource(sheet);
            parser.parse(sheetSource);
            sheet.close();
        }
    }

    public XMLReader fetchSheetParser(StylesTable styles, SharedStringsTable sst)
            throws SAXException {
        XMLReader parser = XMLReaderFactory.createXMLReader();
        //.createXMLReader("org.apache.xerces.parsers.SAXParser");
        parser.setContentHandler(new SheetHandler(styles, sst));
        return parser;
    }


    private class SheetHandler extends DefaultHandler {

        private xssfDataType nextDataType;

        private StylesTable stylesTable;

        private SharedStringsTable sst;

        // Used to format numeric cell values.
        private short formatIndex;
        private String formatString;
        private final DataFormatter formatter;

        // Set when V start element is seen
        private boolean vIsOpen;
        // Gathers characters as they are seen.
        private StringBuffer value;

        private List<String> rowList;
        private int curCol = -1;	//当前列索引
        private int preCol = -1;	//上一列列索引
        private int rowLength = 0;	//列数

        public SheetHandler(StylesTable styles, SharedStringsTable sst) {
            this.stylesTable = styles;
            this.sst = sst;
            this.value = new StringBuffer();
            this.nextDataType = xssfDataType.NUMBER;
            this.formatter = new DataFormatter();
            this.rowList = new ArrayList<>();
        }

        public void startElement(String uri, String localName, String name,
                                 Attributes attributes) throws SAXException {
            if ("inlineStr".equals(name) || "v".equals(name)) {
                vIsOpen = true;
                // Clear contents cache
                value.setLength(0);
            }
            // c => cell
            else if ("c".equals(name)) {
                String rowStr = attributes.getValue("r");
                curCol = this.getRowIndex(rowStr);

                String cellType = attributes.getValue("t");
                String cellStyleStr = attributes.getValue("s");
                this.nextDataType = xssfDataType.NUMBER;
                this.formatIndex = -1;
                this.formatString = null;
                if ("b".equals(cellType))
                    nextDataType = xssfDataType.BOOL;
                else if ("e".equals(cellType))
                    nextDataType = xssfDataType.ERROR;
                else if ("inlineStr".equals(cellType))
                    nextDataType = xssfDataType.INLINESTR;
                else if ("s".equals(cellType))
                    nextDataType = xssfDataType.SSTINDEX;
                else if ("str".equals(cellType))
                    nextDataType = xssfDataType.FORMULA;
                else if (cellStyleStr != null) {
                    // It's a number, but almost certainly one
                    //  with a special style or format
                    int styleIndex = Integer.parseInt(cellStyleStr);
                    XSSFCellStyle style = stylesTable.getStyleAt(styleIndex);
                    this.formatIndex = style.getDataFormat();
                    this.formatString = style.getDataFormatString();
                    if (this.formatString == null)
                        this.formatString = BuiltinFormats.getBuiltinFormat(this.formatIndex);
                }


            }

        }

        public void endElement(String uri, String localName, String name)
                throws SAXException {

            String thisStr = null;

            // v => 单元格的值，如果单元格是字符串则v标签的值为该字符串在SST中的索引
            // 将单元格内容加入rowList中，在这之前先去掉字符串前后的空白符
            if ("v".equals(name)) {

                switch (nextDataType) {

                    case BOOL:
                        char first = value.charAt(0);
                        thisStr = first == '0' ? "0" : "1";
                        break;

                    case ERROR:
                        thisStr = "\"ERROR:" + value.toString() + '"';
                        break;

                    case FORMULA:
                        // A formula could result in a string value,
                        // so always add double-quote characters.
                        thisStr = value.toString();
                        break;

                    case INLINESTR:
                        // have seen an example of this, so it's untested.
                        XSSFRichTextString rtsi = new XSSFRichTextString(value.toString());
                        thisStr = rtsi.toString();
                        break;

                    case SSTINDEX:
                        String sstIndex = value.toString();
                        try {
                            int idx = Integer.parseInt(sstIndex);
                            XSSFRichTextString rtss = new XSSFRichTextString(sst.getEntryAt(idx));
                            thisStr = rtss.toString();
                        }catch (NumberFormatException ex) {
                            ex.printStackTrace();
                            //println("Failed to parse SST index '" + sstIndex + "': " + ex.toString());
                        }
                        break;

                    case NUMBER:
                        String n = value.toString();
                        if (this.formatString != null)
                            thisStr = formatter.formatRawCellContents(Double.parseDouble(n), this.formatIndex, this.formatString);
                        else
                            thisStr = n;
                        break;

                    default:
                        //thisStr = "(Unexpected type: " + nextDataType + ")";
                        thisStr = "";
                        break;
                }

                //当excel中出现空值，则不会执行该方法，所以需要补全
                int cols = curCol-preCol;
                if (cols>1){
                    //System.out.println(curRow+"  "+curCol+"  "+cols);
                    for (int i = 0;i < cols-1;i++){
                        rowList.add(preCol+1, "");
                    }
                }
                preCol = curCol;

                if(thisStr==null){
                    thisStr = "";
                }

                rowList.add(thisStr.trim());

            }else {
                //如果标签名称为 row ，这说明已到行尾，调用 optRows() 方法
                if ("row".equals(name)) {
                    if(curRow == titleRow){
                        this.rowLength = rowList.size();
                    }

                    if(curRow > titleRow){
                        int tmpCols = rowList.size();
                        if(tmpCols < this.rowLength) {
                            for (int i = 0; i < this.rowLength - tmpCols; i++) {
                                rowList.add("");
                            }
                        }
                    }

                    try {
                        operateRows(sheetIndex, curRow, rowList);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    rowList.clear();
                    curRow++;
                    preCol = -1;
                }
            }
        }

        public void characters(char[] ch, int start, int length)
                throws SAXException {
            //得到单元格内容的值
            if (vIsOpen)
                value.append(ch, start, length);
        }

        //得到列索引，每一列c元素的r属性构成为字母加数字的形式，字母组合为列索引，数字组合为行索引，
        //如AB45,表示为第（A-A+1）*26+（B-A+1）*26列，45行
        public int getRowIndex(String rowStr){
            int firstDigit = -1;
            for (int c = 0; c < rowStr.length(); ++c) {
                if (Character.isDigit(rowStr.charAt(c))) {
                    firstDigit = c;
                    break;
                }
            }

            String name = rowStr.substring(0, firstDigit);
            int column = -1;
            for (int i = 0; i < name.length(); ++i) {
                int c = name.charAt(i);
                column = (column + 1) * 26 + c - 'A';
            }
//            System.out.println(column);
//            System.out.println(rowStr);
            return column;
        }

    }

    public int getTitleRow() {
        return titleRow;
    }

    public void setTitleRow(int titleRow) {
        this.titleRow = titleRow;
    }


}
