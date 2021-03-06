package com.mybatis.generator.plugins;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.OutputUtilities;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.ListUtilities;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;

import java.util.*;

import static org.mybatis.generator.internal.util.StringUtility.isTrue;

/**
 * Created by huangzhangting on 2017/5/24.
 */
public class MyPlugin extends PluginAdapter {
    private FullyQualifiedJavaType lombokData;
    private boolean useLombok;

    private final String batchInsert = "batchInsert";
    private final String batchInsertList = "list";


    public MyPlugin() {
        super();
        lombokData = new FullyQualifiedJavaType("lombok.Data");
    }

    public boolean validate(List<String> warnings) {
        /* the plugin always valid */
        return true;
    }

    @Override
    public void setProperties(Properties properties) {
        super.setProperties(properties);
        useLombok = isTrue(properties.getProperty("useLombok"));
        System.out.println("use lombok: "+useLombok);
    }

    @Override
    public boolean modelGetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        if(useLombok)
            return false;
        return super.modelGetterMethodGenerated(method, topLevelClass, introspectedColumn, introspectedTable, modelClassType);
    }

    @Override
    public boolean modelSetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        if(useLombok)
            return false;
        return super.modelGetterMethodGenerated(method, topLevelClass, introspectedColumn, introspectedTable, modelClassType);
    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if(useLombok) {
            Set<FullyQualifiedJavaType> importedTypes = new HashSet<FullyQualifiedJavaType>();
            importedTypes.add(lombokData);
            topLevelClass.addImportedTypes(importedTypes);
            topLevelClass.addAnnotation("@Data");
        }
        return true;
    }

    @Override
    public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        StringBuilder docLine = new StringBuilder();
        docLine.append("/** ");
        String columnRemarks = introspectedColumn.getRemarks();
        if(columnRemarks==null || "".equals(columnRemarks)){
            docLine.append(introspectedColumn.getActualColumnName());
        }else{
            docLine.append(columnRemarks);
        }
        docLine.append(" **/");
        field.addJavaDocLine(docLine.toString());
        return true;
    }

    @Override
    public boolean clientInsertMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return false;
    }

//    @Override
//    public boolean clientInsertMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
//        return false;
//    }

    @Override
    public boolean clientUpdateByPrimaryKeyWithoutBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        generateBatchInsertMethod(interfaze, introspectedTable);
        return true;
    }
    /** 生成批量插入的方法 **/
    private void generateBatchInsertMethod(Interface interfaze, IntrospectedTable introspectedTable){
        Method method = new Method(batchInsert);
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());

        FullyQualifiedJavaType listType = new FullyQualifiedJavaType("List");
        listType.addTypeArgument(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()));
        Parameter parameter = new Parameter(listType, batchInsertList);
        method.addParameter(parameter);

        method.addJavaDocLine("/** batch insert **/");

        interfaze.addMethod(method);
        interfaze.addImportedType(FullyQualifiedJavaType.getNewListInstance()); //导入
    }

    @Override
    public boolean sqlMapInsertElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeyWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        generateBatchInsertSql(document, introspectedTable);
        return true;
    }
    /** 生成批量插入sql **/
    private void generateBatchInsertSql(Document document, IntrospectedTable introspectedTable){
        XmlElement rootElement = document.getRootElement();
        XmlElement insert = new XmlElement("insert");
        insert.addAttribute(new Attribute("id", batchInsert));
        FullyQualifiedJavaType parameterType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        insert.addAttribute(new Attribute("parameterType", parameterType.getFullyQualifiedName()));

        StringBuilder insertClause = new StringBuilder();
        StringBuilder valuesClause = new StringBuilder();

        insertClause.append("insert into ");
        insertClause.append(introspectedTable.getFullyQualifiedTableNameAtRuntime());
        insertClause.append(" (");
        insert.addElement(new TextElement(insertClause.toString()));
        insertClause.setLength(0);
        OutputUtilities.xmlIndent(insertClause, 1); //缩进

        String foreachItem = "item";
        List<String> valuesClauseList = new ArrayList<String>();
        List<IntrospectedColumn> columnList = ListUtilities.removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getAllColumns());
        int size = columnList.size();
        for(int i=0; i<size; i++){
            IntrospectedColumn introspectedColumn = columnList.get(i);
            insertClause.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
            valuesClause.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn, foreachItem+"."));
            if (i + 1 < size) {
                insertClause.append(", ");
                valuesClause.append(", ");
            }
            if(valuesClause.length() > 80){ //避免单行字符过长
                insert.addElement(new TextElement(insertClause.toString())); //insert语句，直接添加
                insertClause.setLength(0);
                OutputUtilities.xmlIndent(insertClause, 1); //缩进

                valuesClauseList.add(valuesClause.toString());
                valuesClause.setLength(0);
//                OutputUtilities.xmlIndent(valuesClause, 1);
            }
        }
        //因为每行 >80 就直接添加了，所以还剩最后一行数据
        valuesClauseList.add(valuesClause.toString());
        insertClause.append(")");
        insert.addElement(new TextElement(insertClause.toString()));

        insert.addElement(new TextElement("values"));

        XmlElement foreach = new XmlElement("foreach");
        foreach.addAttribute(new Attribute("collection", batchInsertList));
        foreach.addAttribute(new Attribute("item", foreachItem));
        foreach.addAttribute(new Attribute("index", "index"));
        foreach.addAttribute(new Attribute("separator", ","));
        foreach.addElement(new TextElement("("));
        for(String val : valuesClauseList){
            foreach.addElement(new TextElement(val));
        }
        foreach.addElement(new TextElement(")"));

        insert.addElement(foreach);

        StringBuilder note = new StringBuilder();
        OutputUtilities.newLine(note);
        OutputUtilities.xmlIndent(note, 1);
        note.append("<!-- batch insert -->");
        rootElement.addElement(new TextElement(note.toString()));
        rootElement.addElement(insert);
    }

}
