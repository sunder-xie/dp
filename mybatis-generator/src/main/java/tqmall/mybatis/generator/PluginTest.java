package tqmall.mybatis.generator;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by huangzhangting on 17/4/11.
 */
public class PluginTest extends PluginAdapter {
    private FullyQualifiedJavaType lombokData;

    public PluginTest() {
        super();
        lombokData = new FullyQualifiedJavaType("lombok.Data");
    }

    public boolean validate(List<String> warnings) {
        /* the plugin always valid */
        return true;
    }

    @Override
    public boolean modelGetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        return false;
    }

    @Override
    public boolean modelSetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        return false;
    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        Set<FullyQualifiedJavaType> importedTypes = new HashSet<FullyQualifiedJavaType>();
        importedTypes.add(lombokData);
        topLevelClass.addImportedTypes(importedTypes);
        topLevelClass.addAnnotation("@Data");
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
        Method method = new Method("batchInsert");
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());

        FullyQualifiedJavaType listType = FullyQualifiedJavaType.getNewListInstance();
        listType.addTypeArgument(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()));
        Parameter parameter = new Parameter(listType, "list");
        method.addParameter(parameter);

        method.addJavaDocLine("/** 批量插入 **/");

        interfaze.addMethod(method);
        interfaze.addImportedType(listType);
    }

    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        XmlElement rootElement = document.getRootElement();
        XmlElement insert = new XmlElement("insert");

        return true;
    }
}
