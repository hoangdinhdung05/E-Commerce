package com.training.demo.helpers.Reports;

import com.training.demo.dto.response.Product.ExportProductResponse;
import com.training.demo.dto.response.User.ExportUserResponse;
import com.training.demo.dto.response.Order.ExportOrderResponse;
import com.training.demo.dto.response.Payment.ExportPaymentResponse;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.*;
import net.sf.jasperreports.engine.type.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;

@Slf4j
public class JasperReportGenerator {

    private JasperReport userReportTemplate;
    private JasperReport productReportTemplate;
    private JasperReport orderReportTemplate;
    private JasperReport paymentReportTemplate;

    /**
     * Constructor - Compile templates 1 lần khi khởi tạo
     */
    public JasperReportGenerator() {
        try {
            log.info("[JasperReportGenerator] Compiling User Report Template...");
            this.userReportTemplate = compileUserReportTemplate();
            log.info("[JasperReportGenerator] User Report Template compiled successfully");

            log.info("[JasperReportGenerator] Compiling Product Report Template...");
            this.productReportTemplate = compileProductReportTemplate();
            log.info("[JasperReportGenerator] Product Report Template compiled successfully");

            log.info("[JasperReportGenerator] Compiling Order Report Template...");
            this.orderReportTemplate = compileOrderReportTemplate();
            log.info("[JasperReportGenerator] Order Report Template compiled successfully");

            log.info("[JasperReportGenerator] Compiling Payment Report Template...");
            this.paymentReportTemplate = compilePaymentReportTemplate();
            log.info("[JasperReportGenerator] Payment Report Template compiled successfully");
        } catch (JRException e) {
            log.error("[JasperReportGenerator] Failed to compile templates", e);
            throw new RuntimeException("Failed to initialize JasperReportGenerator", e);
        }
    }

    /**
     * Generate User Report - Chỉ fill data vào template đã compile
     */
    public JasperPrint generateUserReport(List<ExportUserResponse> users) throws JRException {
        log.debug("[JasperReportGenerator] Filling user report with {} records", users.size());
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(users);
        return JasperFillManager.fillReport(userReportTemplate, new HashMap<>(), dataSource);
    }

    /**
     * Generate Product Report - Chỉ fill data vào template đã compile
     */
    public JasperPrint generateProductReport(List<ExportProductResponse> products) throws JRException {
        log.debug("[JasperReportGenerator] Filling product report with {} records", products.size());
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(products);
        return JasperFillManager.fillReport(productReportTemplate, new HashMap<>(), dataSource);
    }

    /**
     * Generate Order Report - Chỉ fill data vào template đã compile
     */
    public JasperPrint generateOrderReport(List<ExportOrderResponse> orders) throws JRException {
        log.debug("[JasperReportGenerator] Filling order report with {} records", orders.size());
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(orders);
        return JasperFillManager.fillReport(orderReportTemplate, new HashMap<>(), dataSource);
    }

    /**
     * Generate Payment Report - Chỉ fill data vào template đã compile
     */
    public JasperPrint generatePaymentReport(List<ExportPaymentResponse> payments) throws JRException {
        log.debug("[JasperReportGenerator] Filling payment report with {} records", payments.size());
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(payments);
        return JasperFillManager.fillReport(paymentReportTemplate, new HashMap<>(), dataSource);
    }

    // ============================================================
    // PRIVATE: Compile templates (chỉ chạy 1 lần khi init)
    // ============================================================

    private JasperReport compileUserReportTemplate() throws JRException {
        JasperDesign jasperDesign = new JasperDesign();
        jasperDesign.setName("user_report");
        jasperDesign.setPageWidth(595);
        jasperDesign.setPageHeight(842);
        jasperDesign.setColumnWidth(555);
        jasperDesign.setLeftMargin(20);
        jasperDesign.setRightMargin(20);
        jasperDesign.setTopMargin(20);
        jasperDesign.setBottomMargin(20);

        // ====== Fields ======
        String[][] fields = {
                {"id", "java.lang.Long"},
                {"firstName", "java.lang.String"},
                {"lastName", "java.lang.String"},
                {"username", "java.lang.String"},
                {"email", "java.lang.String"},
                {"status", "java.lang.String"},
                {"roleName", "java.lang.String"}
        };
        for (String[] f : fields) {
            JRDesignField field = new JRDesignField();
            field.setName(f[0]);
            field.setValueClassName(f[1]);
            jasperDesign.addField(field);
        }

        // ====== Title ======
        JRDesignBand titleBand = new JRDesignBand();
        titleBand.setHeight(50);

        JRDesignStaticText titleText = new JRDesignStaticText();
        titleText.setX(0);
        titleText.setY(10);
        titleText.setWidth(555);
        titleText.setHeight(30);
        titleText.setText("USER REPORT");
        titleText.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        titleText.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
        titleText.setFontSize(20f);
        titleText.setBold(true);
        titleText.setForecolor(new Color(40, 40, 40));

        titleBand.addElement(titleText);
        jasperDesign.setTitle(titleBand);

        // ====== Column Header ======
        JRDesignBand columnHeader = new JRDesignBand();
        columnHeader.setHeight(25);

        int x = 0;
        columnHeader.addElement(createHeaderCell("ID", x, 35)); x += 35;
        columnHeader.addElement(createHeaderCell("First Name", x, 90)); x += 90;
        columnHeader.addElement(createHeaderCell("Last Name", x, 90)); x += 90;
        columnHeader.addElement(createHeaderCell("Username", x, 90)); x += 90;
        columnHeader.addElement(createHeaderCell("Email", x, 130)); x += 130;
        columnHeader.addElement(createHeaderCell("Status", x, 60)); x += 60;
        columnHeader.addElement(createHeaderCell("Role", x, 60));
        jasperDesign.setColumnHeader(columnHeader);

        // ====== Detail ======
        JRDesignBand detailBand = new JRDesignBand();
        detailBand.setHeight(20);

        int dx = 0;
        detailBand.addElement(createDetailCell("($F{id} == null) ? \"—\" : String.valueOf($F{id})", dx, 35)); dx += 35;
        detailBand.addElement(createDetailCell("($F{firstName} == null) ? \"—\" : $F{firstName}", dx, 90)); dx += 90;
        detailBand.addElement(createDetailCell("($F{lastName} == null) ? \"—\" : $F{lastName}", dx, 90)); dx += 90;
        detailBand.addElement(createDetailCell("($F{username} == null) ? \"—\" : $F{username}", dx, 90)); dx += 90;
        detailBand.addElement(createDetailCell("($F{email} == null) ? \"—\" : $F{email}", dx, 130)); dx += 130;

        // STATUS có conditional color
        JRDesignTextField statusField = createDetailCell("($F{status} == null) ? \"—\" : $F{status}", dx, 60);
        applyConditionalStatusColor(jasperDesign, statusField);
        detailBand.addElement(statusField);
        dx += 60;

        // ROLE
        detailBand.addElement(createDetailCell("($F{roleName} == null) ? \"—\" : $F{roleName}", dx, 60));

        ((JRDesignSection) jasperDesign.getDetailSection()).addBand(detailBand);

        // ====== Footer ======
        JRDesignBand footerBand = new JRDesignBand();
        footerBand.setHeight(25);

        JRDesignTextField footerText = new JRDesignTextField();
        footerText.setX(0);
        footerText.setY(0);
        footerText.setWidth(555);
        footerText.setHeight(20);
        footerText.setExpression(new JRDesignExpression("\"Generated by Admin | Page \" + $V{PAGE_NUMBER}"));
        footerText.setHorizontalTextAlign(HorizontalTextAlignEnum.RIGHT);
        footerText.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
        footerText.setFontSize(9f);
        footerText.setForecolor(Color.DARK_GRAY);

        footerBand.addElement(footerText);
        jasperDesign.setPageFooter(footerBand);

        // ====== Compile ONLY (không fill data) ======
        return JasperCompileManager.compileReport(jasperDesign);
    }

    private JasperReport compileProductReportTemplate() throws JRException {
        JasperDesign jasperDesign = new JasperDesign();
        jasperDesign.setName("product_report");
        jasperDesign.setPageWidth(595);
        jasperDesign.setPageHeight(842);
        jasperDesign.setColumnWidth(555);
        jasperDesign.setLeftMargin(20);
        jasperDesign.setRightMargin(20);
        jasperDesign.setTopMargin(20);
        jasperDesign.setBottomMargin(20);

        // ====== Fields ======
        String[][] fields = {
                {"id", "java.lang.Long"},
                {"name", "java.lang.String"},
                {"description", "java.lang.String"},
                {"price", "java.lang.Double"},
                {"quantity", "java.lang.Integer"},
                {"categoryName", "java.lang.String"},
                {"createdAt", "java.time.LocalDateTime"}
        };
        for (String[] f : fields) {
            JRDesignField field = new JRDesignField();
            field.setName(f[0]);
            field.setValueClassName(f[1]);
            jasperDesign.addField(field);
        }

        // ====== Title ======
        JRDesignBand titleBand = new JRDesignBand();
        titleBand.setHeight(50);

        JRDesignStaticText titleText = new JRDesignStaticText();
        titleText.setX(0);
        titleText.setY(10);
        titleText.setWidth(555);
        titleText.setHeight(30);
        titleText.setText("PRODUCT CATALOG REPORT");
        titleText.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        titleText.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
        titleText.setFontSize(20f);
        titleText.setBold(true);
        titleText.setForecolor(new Color(40, 40, 40));

        titleBand.addElement(titleText);
        jasperDesign.setTitle(titleBand);

        // ====== Column Header ======
        JRDesignBand columnHeader = new JRDesignBand();
        columnHeader.setHeight(25);

        int x = 0;
        columnHeader.addElement(createHeaderCell("ID", x, 35));           x += 35;
        columnHeader.addElement(createHeaderCell("Name", x, 95));         x += 95;
        columnHeader.addElement(createHeaderCell("Description", x, 130)); x += 130;
        columnHeader.addElement(createHeaderCell("Price", x, 65));        x += 65;
        columnHeader.addElement(createHeaderCell("Qty", x, 50));          x += 50;
        columnHeader.addElement(createHeaderCell("Category", x, 100));    x += 100;
        columnHeader.addElement(createHeaderCell("Created At", x, 80));   // x kết thúc = 555
        jasperDesign.setColumnHeader(columnHeader);

        // ====== Detail ======
        JRDesignBand detailBand = new JRDesignBand();
        detailBand.setHeight(65);

        int dx = 0;

        // ID
        detailBand.addElement(createProductDetailCell(
                "($F{id} == null) ? \"—\" : String.valueOf($F{id})", dx, 35, 65));
        dx += 35;

        // Name
        detailBand.addElement(createProductDetailCell(
                "($F{name} == null) ? \"—\" : $F{name}", dx, 95, 65));
        dx += 95;

        // Description
        JRDesignTextField descField = createProductDetailCell(
                "($F{description} == null) ? \"—\" : $F{description}", dx, 130, 65);
        descField.setStretchType(StretchTypeEnum.RELATIVE_TO_TALLEST_OBJECT);
        descField.setFontSize(9f);
        detailBand.addElement(descField);
        dx += 130;

        // Price
        detailBand.addElement(createProductDetailCell(
                "($F{price} == null) ? \"—\" : String.format(\"$%.2f\", $F{price})",
                dx, 65, 65));
        dx += 65;

        // Quantity
        JRDesignTextField qtyField = createProductDetailCell(
                "($F{quantity} == null) ? \"—\" : String.valueOf($F{quantity})",
                dx, 50, 65);
        applyConditionalQuantityColor(jasperDesign, qtyField);
        detailBand.addElement(qtyField);
        dx += 50;

        // Category
        detailBand.addElement(createProductDetailCell(
                "($F{categoryName} == null) ? \"—\" : $F{categoryName}", dx, 100, 65));
        dx += 100;

        // Created At (format gọn dd/MM/yyyy HH:mm)
        JRDesignTextField createdAtField = createProductDetailCell(
                "($F{createdAt} == null) ? \"—\" : " +
                        "$F{createdAt}.format(java.time.format.DateTimeFormatter.ofPattern(\"dd/MM/yyyy HH:mm\"))",
                dx, 80, 65);
        detailBand.addElement(createdAtField);

        // add band
        ((JRDesignSection) jasperDesign.getDetailSection()).addBand(detailBand);

        // ====== Footer ======
        JRDesignBand footerBand = new JRDesignBand();
        footerBand.setHeight(25);

        JRDesignTextField footerText = new JRDesignTextField();
        footerText.setX(0);
        footerText.setY(0);
        footerText.setWidth(555);
        footerText.setHeight(20);
        footerText.setExpression(new JRDesignExpression("\"Generated by Admin | Page \" + $V{PAGE_NUMBER}"));
        footerText.setHorizontalTextAlign(HorizontalTextAlignEnum.RIGHT);
        footerText.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
        footerText.setFontSize(9f);
        footerText.setForecolor(Color.DARK_GRAY);

        footerBand.addElement(footerText);
        jasperDesign.setPageFooter(footerBand);

        // ====== Compile ONLY (không fill data) ======
        return JasperCompileManager.compileReport(jasperDesign);
    }

    private JRDesignStaticText createHeaderCell(String text, int x, int width) {
        JRDesignStaticText header = new JRDesignStaticText();
        header.setX(x);
        header.setY(0);
        header.setWidth(width);
        header.setHeight(25);
        header.setText(text);
        header.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        header.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
        header.setFontSize(12f);
        header.setBold(true);
        header.setBackcolor(new Color(220, 230, 250));
        header.setMode(ModeEnum.OPAQUE);
        header.getLineBox().getPen().setLineWidth(0.5f);
        return header;
    }

    private JRDesignTextField createDetailCell(String expression, int x, int width) {
        JRDesignTextField field = new JRDesignTextField();
        field.setX(x);
        field.setY(0);
        field.setWidth(width);
        field.setHeight(20);
        field.setExpression(new JRDesignExpression(expression));
        field.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        field.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
        field.setFontSize(10.5f);
        field.getLineBox().getPen().setLineWidth(0.25f);
        field.getLineBox().getPen().setLineColor(new Color(180, 180, 180));
        return field;
    }

    private JRDesignTextField createProductDetailCell(String expression, int x, int width, int height) {
        JRDesignTextField field = new JRDesignTextField();
        field.setX(x);
        field.setY(0);
        field.setWidth(width);
        field.setHeight(height);
        field.setExpression(new JRDesignExpression(expression));
        field.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        field.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
        field.setFontSize(10f);
        field.getLineBox().getPen().setLineWidth(0.25f);
        field.getLineBox().getPen().setLineColor(new Color(180, 180, 180));
        return field;
    }

    private void applyConditionalStatusColor(JasperDesign design, JRDesignTextField field) throws JRException {
        JRDesignStyle style = new JRDesignStyle();
        style.setName("StatusStyle");

        JRDesignConditionalStyle active = new JRDesignConditionalStyle();
        active.setConditionExpression(new JRDesignExpression("$F{status} != null && $F{status}.equals(\"ACTIVE\")"));
        active.setBackcolor(new Color(198, 239, 206));
        active.setMode(ModeEnum.OPAQUE);

        JRDesignConditionalStyle inactive = new JRDesignConditionalStyle();
        inactive.setConditionExpression(new JRDesignExpression("$F{status} != null && $F{status}.equals(\"INACTIVE\")"));
        inactive.setBackcolor(new Color(255, 235, 156));
        inactive.setMode(ModeEnum.OPAQUE);

        JRDesignConditionalStyle banned = new JRDesignConditionalStyle();
        banned.setConditionExpression(new JRDesignExpression("$F{status} != null && $F{status}.equals(\"BANNED\")"));
        banned.setBackcolor(new Color(244, 204, 204));
        banned.setMode(ModeEnum.OPAQUE);

        style.addConditionalStyle(active);
        style.addConditionalStyle(inactive);
        style.addConditionalStyle(banned);
        design.addStyle(style);

        field.setStyle(style);
    }

    private void applyConditionalQuantityColor(JasperDesign design, JRDesignTextField field) throws JRException {
        JRDesignStyle style = new JRDesignStyle();
        style.setName("QuantityStyle");

        // Low stock (< 10): Red background
        JRDesignConditionalStyle lowStock = new JRDesignConditionalStyle();
        lowStock.setConditionExpression(new JRDesignExpression("$F{quantity} != null && $F{quantity} < 10"));
        lowStock.setBackcolor(new Color(244, 204, 204));
        lowStock.setForecolor(new Color(139, 0, 0));
        lowStock.setMode(ModeEnum.OPAQUE);
        lowStock.setBold(true);

        // Medium stock (10-50): Yellow background
        JRDesignConditionalStyle mediumStock = new JRDesignConditionalStyle();
        mediumStock.setConditionExpression(new JRDesignExpression("$F{quantity} != null && $F{quantity} >= 10 && $F{quantity} <= 50"));
        mediumStock.setBackcolor(new Color(255, 235, 156));
        mediumStock.setMode(ModeEnum.OPAQUE);

        // High stock (> 50): Green background
        JRDesignConditionalStyle highStock = new JRDesignConditionalStyle();
        highStock.setConditionExpression(new JRDesignExpression("$F{quantity} != null && $F{quantity} > 50"));
        highStock.setBackcolor(new Color(198, 239, 206));
        highStock.setForecolor(new Color(0, 100, 0));
        highStock.setMode(ModeEnum.OPAQUE);

        style.addConditionalStyle(lowStock);
        style.addConditionalStyle(mediumStock);
        style.addConditionalStyle(highStock);
        design.addStyle(style);

        field.setStyle(style);
    }

    // ============================================================
    // ORDER REPORT TEMPLATE
    // ============================================================

    private JasperReport compileOrderReportTemplate() throws JRException {
        JasperDesign jasperDesign = new JasperDesign();
        jasperDesign.setName("order_report");
        jasperDesign.setPageWidth(842);  // A4 Landscape
        jasperDesign.setPageHeight(595);
        jasperDesign.setColumnWidth(802);
        jasperDesign.setLeftMargin(20);
        jasperDesign.setRightMargin(20);
        jasperDesign.setTopMargin(20);
        jasperDesign.setBottomMargin(20);

        // ====== Fields ======
        String[][] fields = {
                {"id", "java.lang.Long"},
                {"orderNumber", "java.lang.String"},
                {"username", "java.lang.String"},
                {"receiverName", "java.lang.String"},
                {"phoneNumber", "java.lang.String"},
                {"shippingAddress", "java.lang.String"},
                {"totalAmount", "java.math.BigDecimal"},
                {"status", "java.lang.String"},
                {"paymentMethod", "java.lang.String"},
                {"paymentStatus", "java.lang.String"},
                {"createdAt", "java.time.LocalDateTime"},
                {"note", "java.lang.String"}
        };
        for (String[] f : fields) {
            JRDesignField field = new JRDesignField();
            field.setName(f[0]);
            field.setValueClassName(f[1]);
            jasperDesign.addField(field);
        }

        // ====== Title ======
        JRDesignBand titleBand = new JRDesignBand();
        titleBand.setHeight(50);

        JRDesignStaticText titleText = new JRDesignStaticText();
        titleText.setX(0);
        titleText.setY(10);
        titleText.setWidth(802);
        titleText.setHeight(30);
        titleText.setText("ORDER MANAGEMENT REPORT");
        titleText.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        titleText.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
        titleText.setFontSize(20f);
        titleText.setBold(true);
        titleText.setForecolor(new Color(40, 40, 40));

        titleBand.addElement(titleText);
        jasperDesign.setTitle(titleBand);

        // ====== Column Header ======
        JRDesignBand columnHeader = new JRDesignBand();
        columnHeader.setHeight(25);

        int x = 0;
        columnHeader.addElement(createHeaderCell("ID", x, 35));            x += 35;
        columnHeader.addElement(createHeaderCell("Order #", x, 140));      x += 140;
        columnHeader.addElement(createHeaderCell("Customer", x, 90));     x += 90;
        columnHeader.addElement(createHeaderCell("Receiver", x, 90));     x += 90;
        columnHeader.addElement(createHeaderCell("Phone", x, 85));         x += 85;
        columnHeader.addElement(createHeaderCell("Amount", x, 75));        x += 75;
        columnHeader.addElement(createHeaderCell("Status", x, 75));        x += 75;
        columnHeader.addElement(createHeaderCell("Payment", x, 75));       x += 75;
        columnHeader.addElement(createHeaderCell("P.Status", x, 67));      x += 67;
        columnHeader.addElement(createHeaderCell("Created", x, 70));
        jasperDesign.setColumnHeader(columnHeader);

        // ====== Detail ======
        JRDesignBand detailBand = new JRDesignBand();
        detailBand.setHeight(20);

        int dx = 0;
        detailBand.addElement(createDetailCell("($F{id} == null) ? \"—\" : String.valueOf($F{id})", dx, 35)); dx += 35;
        detailBand.addElement(createDetailCell("($F{orderNumber} == null) ? \"—\" : $F{orderNumber}", dx, 140)); dx += 140;
        detailBand.addElement(createDetailCell("($F{username} == null) ? \"—\" : $F{username}", dx, 90)); dx += 90;
        detailBand.addElement(createDetailCell("($F{receiverName} == null) ? \"—\" : $F{receiverName}", dx, 90)); dx += 90;
        detailBand.addElement(createDetailCell("($F{phoneNumber} == null) ? \"—\" : $F{phoneNumber}", dx, 85)); dx += 85;
        detailBand.addElement(createDetailCell("($F{totalAmount} == null) ? \"—\" : \"$\" + String.format(\"%.2f\", $F{totalAmount}.doubleValue())", dx, 75)); dx += 75;

        // Order Status with conditional color
        JRDesignTextField orderStatusField = createDetailCell("($F{status} == null) ? \"—\" : $F{status}", dx, 75);
        applyConditionalOrderStatusColor(jasperDesign, orderStatusField);
        detailBand.addElement(orderStatusField);
        dx += 75;

        detailBand.addElement(createDetailCell("($F{paymentMethod} == null) ? \"—\" : $F{paymentMethod}", dx, 75)); dx += 75;

        // Payment Status with conditional color
        JRDesignTextField paymentStatusField = createDetailCell("($F{paymentStatus} == null) ? \"—\" : $F{paymentStatus}", dx, 67);
        applyConditionalPaymentStatusColor(jasperDesign, paymentStatusField);
        detailBand.addElement(paymentStatusField);
        dx += 67;

        // Created Date
        detailBand.addElement(createDetailCell(
                "($F{createdAt} == null) ? \"—\" : $F{createdAt}.format(java.time.format.DateTimeFormatter.ofPattern(\"dd/MM/yyyy\"))",
                dx, 70));

        ((JRDesignSection) jasperDesign.getDetailSection()).addBand(detailBand);

        // ====== Footer ======
        JRDesignBand footerBand = new JRDesignBand();
        footerBand.setHeight(25);

        JRDesignTextField footerText = new JRDesignTextField();
        footerText.setX(0);
        footerText.setY(0);
        footerText.setWidth(802);
        footerText.setHeight(20);
        footerText.setExpression(new JRDesignExpression("\"Generated by Admin | Page \" + $V{PAGE_NUMBER}"));
        footerText.setHorizontalTextAlign(HorizontalTextAlignEnum.RIGHT);
        footerText.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
        footerText.setFontSize(9f);
        footerText.setForecolor(Color.DARK_GRAY);

        footerBand.addElement(footerText);
        jasperDesign.setPageFooter(footerBand);

        return JasperCompileManager.compileReport(jasperDesign);
    }

    // ============================================================
    // PAYMENT REPORT TEMPLATE
    // ============================================================

    private JasperReport compilePaymentReportTemplate() throws JRException {
        JasperDesign jasperDesign = new JasperDesign();
        jasperDesign.setName("payment_report");
        jasperDesign.setPageWidth(842);  // A4 Landscape
        jasperDesign.setPageHeight(595);
        jasperDesign.setColumnWidth(802);
        jasperDesign.setLeftMargin(20);
        jasperDesign.setRightMargin(20);
        jasperDesign.setTopMargin(20);
        jasperDesign.setBottomMargin(20);

        // ====== Fields ======
        String[][] fields = {
                {"id", "java.lang.Long"},
                {"orderId", "java.lang.Long"},
                {"orderNumber", "java.lang.String"},
                {"username", "java.lang.String"},
                {"paymentMethod", "java.lang.String"},
                {"amount", "java.math.BigDecimal"},
                {"status", "java.lang.String"},
                {"transactionId", "java.lang.String"},
                {"paymentDate", "java.time.LocalDateTime"},
                {"paymentInfo", "java.lang.String"}
        };
        for (String[] f : fields) {
            JRDesignField field = new JRDesignField();
            field.setName(f[0]);
            field.setValueClassName(f[1]);
            jasperDesign.addField(field);
        }

        // ====== Title ======
        JRDesignBand titleBand = new JRDesignBand();
        titleBand.setHeight(50);

        JRDesignStaticText titleText = new JRDesignStaticText();
        titleText.setX(0);
        titleText.setY(10);
        titleText.setWidth(802);
        titleText.setHeight(30);
        titleText.setText("PAYMENT TRANSACTION REPORT");
        titleText.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        titleText.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
        titleText.setFontSize(20f);
        titleText.setBold(true);
        titleText.setForecolor(new Color(40, 40, 40));

        titleBand.addElement(titleText);
        jasperDesign.setTitle(titleBand);

        // ====== Column Header ======
        JRDesignBand columnHeader = new JRDesignBand();
        columnHeader.setHeight(25);

        int x = 0;
        columnHeader.addElement(createHeaderCell("ID", x, 40));           x += 40;
        columnHeader.addElement(createHeaderCell("Order ID", x, 60));    x += 60;
        columnHeader.addElement(createHeaderCell("Order #", x, 90));     x += 90;
        columnHeader.addElement(createHeaderCell("Username", x, 120));    x += 120;
        columnHeader.addElement(createHeaderCell("Method", x, 100));      x += 100;
        columnHeader.addElement(createHeaderCell("Amount", x, 90));       x += 90;
        columnHeader.addElement(createHeaderCell("Status", x, 80));       x += 80;
        columnHeader.addElement(createHeaderCell("Transaction ID", x, 122)); x += 122;
        columnHeader.addElement(createHeaderCell("Payment Date", x, 100));
        jasperDesign.setColumnHeader(columnHeader);

        // ====== Detail ======
        JRDesignBand detailBand = new JRDesignBand();
        detailBand.setHeight(20);

        int dx = 0;
        detailBand.addElement(createDetailCell("($F{id} == null) ? \"—\" : String.valueOf($F{id})", dx, 40)); dx += 40;
        detailBand.addElement(createDetailCell("($F{orderId} == null) ? \"—\" : String.valueOf($F{orderId})", dx, 60)); dx += 60;
        detailBand.addElement(createDetailCell("($F{orderNumber} == null) ? \"—\" : $F{orderNumber}", dx, 90)); dx += 90;
        detailBand.addElement(createDetailCell("($F{username} == null) ? \"—\" : $F{username}", dx, 120)); dx += 120;
        detailBand.addElement(createDetailCell("($F{paymentMethod} == null) ? \"—\" : $F{paymentMethod}", dx, 100)); dx += 100;
        detailBand.addElement(createDetailCell("($F{amount} == null) ? \"—\" : \"$\" + String.format(\"%.2f\", $F{amount}.doubleValue())", dx, 90)); dx += 90;

        // Payment Status with conditional color
        JRDesignTextField statusField = createDetailCell("($F{status} == null) ? \"—\" : $F{status}", dx, 80);
        applyConditionalPaymentStatusColor(jasperDesign, statusField);
        detailBand.addElement(statusField);
        dx += 80;

        detailBand.addElement(createDetailCell("($F{transactionId} == null) ? \"—\" : $F{transactionId}", dx, 122)); dx += 122;
        detailBand.addElement(createDetailCell(
                "($F{paymentDate} == null) ? \"—\" : $F{paymentDate}.format(java.time.format.DateTimeFormatter.ofPattern(\"dd/MM/yyyy HH:mm\"))",
                dx, 100));

        ((JRDesignSection) jasperDesign.getDetailSection()).addBand(detailBand);

        // ====== Footer ======
        JRDesignBand footerBand = new JRDesignBand();
        footerBand.setHeight(25);

        JRDesignTextField footerText = new JRDesignTextField();
        footerText.setX(0);
        footerText.setY(0);
        footerText.setWidth(802);
        footerText.setHeight(20);
        footerText.setExpression(new JRDesignExpression("\"Generated by Admin | Page \" + $V{PAGE_NUMBER}"));
        footerText.setHorizontalTextAlign(HorizontalTextAlignEnum.RIGHT);
        footerText.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
        footerText.setFontSize(9f);
        footerText.setForecolor(Color.DARK_GRAY);

        footerBand.addElement(footerText);
        jasperDesign.setPageFooter(footerBand);

        return JasperCompileManager.compileReport(jasperDesign);
    }

    // ============================================================
    // CONDITIONAL STYLES FOR ORDER & PAYMENT
    // ============================================================

    private void applyConditionalOrderStatusColor(JasperDesign design, JRDesignTextField field) throws JRException {
        JRDesignStyle style = new JRDesignStyle();
        style.setName("OrderStatusStyle");

        JRDesignConditionalStyle pending = new JRDesignConditionalStyle();
        pending.setConditionExpression(new JRDesignExpression("$F{status} != null && $F{status}.equals(\"PENDING\")"));
        pending.setBackcolor(new Color(255, 243, 205));
        pending.setMode(ModeEnum.OPAQUE);

        JRDesignConditionalStyle confirmed = new JRDesignConditionalStyle();
        confirmed.setConditionExpression(new JRDesignExpression("$F{status} != null && $F{status}.equals(\"CONFIRMED\")"));
        confirmed.setBackcolor(new Color(209, 236, 241));
        confirmed.setMode(ModeEnum.OPAQUE);

        JRDesignConditionalStyle shipping = new JRDesignConditionalStyle();
        shipping.setConditionExpression(new JRDesignExpression("$F{status} != null && $F{status}.equals(\"SHIPPING\")"));
        shipping.setBackcolor(new Color(187, 222, 251));
        shipping.setMode(ModeEnum.OPAQUE);

        JRDesignConditionalStyle delivered = new JRDesignConditionalStyle();
        delivered.setConditionExpression(new JRDesignExpression("$F{status} != null && $F{status}.equals(\"DELIVERED\")"));
        delivered.setBackcolor(new Color(198, 239, 206));
        delivered.setMode(ModeEnum.OPAQUE);

        JRDesignConditionalStyle cancelled = new JRDesignConditionalStyle();
        cancelled.setConditionExpression(new JRDesignExpression("$F{status} != null && $F{status}.equals(\"CANCELLED\")"));
        cancelled.setBackcolor(new Color(244, 204, 204));
        cancelled.setMode(ModeEnum.OPAQUE);

        style.addConditionalStyle(pending);
        style.addConditionalStyle(confirmed);
        style.addConditionalStyle(shipping);
        style.addConditionalStyle(delivered);
        style.addConditionalStyle(cancelled);
        design.addStyle(style);

        field.setStyle(style);
    }

    private void applyConditionalPaymentStatusColor(JasperDesign design, JRDesignTextField field) throws JRException {
        JRDesignStyle style = new JRDesignStyle();
        style.setName("PaymentStatusStyle");

        JRDesignConditionalStyle paid = new JRDesignConditionalStyle();
        paid.setConditionExpression(new JRDesignExpression("$F{status} != null && ($F{status}.equals(\"PAID\") || $F{paymentStatus}.equals(\"PAID\"))"));
        paid.setBackcolor(new Color(198, 239, 206));
        paid.setForecolor(new Color(0, 100, 0));
        paid.setMode(ModeEnum.OPAQUE);
        paid.setBold(true);

        JRDesignConditionalStyle pending = new JRDesignConditionalStyle();
        pending.setConditionExpression(new JRDesignExpression("$F{status} != null && ($F{status}.equals(\"PENDING\") || $F{paymentStatus}.equals(\"PENDING\"))"));
        pending.setBackcolor(new Color(255, 243, 205));
        pending.setMode(ModeEnum.OPAQUE);

        JRDesignConditionalStyle failed = new JRDesignConditionalStyle();
        failed.setConditionExpression(new JRDesignExpression("$F{status} != null && ($F{status}.equals(\"FAILED\") || $F{paymentStatus}.equals(\"FAILED\"))"));
        failed.setBackcolor(new Color(244, 204, 204));
        failed.setForecolor(new Color(139, 0, 0));
        failed.setMode(ModeEnum.OPAQUE);

        style.addConditionalStyle(paid);
        style.addConditionalStyle(pending);
        style.addConditionalStyle(failed);
        design.addStyle(style);

        field.setStyle(style);
    }
}