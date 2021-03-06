package maxmoto1702.excel

import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFFont
import spock.lang.*

class ExcelBuilderTests extends Specification {

    def "test types"() {
        setup:
        def builder = new ExcelBuilder()
        def date = new Date()

        when:
        def workbook = builder.build {
            sheet(name: "Demo types") {
                row {
                    cell {
                        "string value"
                    }
                    cell {
                        // integer value
                        1
                    }
                    cell {
                        // date value
                        date
                    }
                    cell {
                        "dyna" + "mic"
                    }
                }
            }
        }

        then:
        workbook.getSheet("Demo types") != null
        workbook.getSheet("Demo types").getRow(0) != null
        workbook.getSheet("Demo types").getRow(0).getCell(0)?.stringCellValue == "string value"
        workbook.getSheet("Demo types").getRow(0).getCell(1)?.numericCellValue == 1
        workbook.getSheet("Demo types").getRow(0).getCell(2)?.dateCellValue == date
        workbook.getSheet("Demo types").getRow(0).getCell(3)?.stringCellValue == "dynamic"
    }

    def "test styles"() {
        setup:
        def builder = new ExcelBuilder()

        when:
        builder.config {
            style('commonStyle') { cellStyle ->
                cellStyle.alignment = CellStyle.ALIGN_CENTER
                cellStyle.borderBottom = CellStyle.BORDER_DASH_DOT
            }
            style('customStyle') { cellStyle ->
                cellStyle.alignment = CellStyle.ALIGN_LEFT
                cellStyle.borderTop = CellStyle.BORDER_DOUBLE
            }
        }
        def workbook = builder.build {
            sheet(name: "Demo styles") {
                row(style: 'commonStyle') {
                    cell {
                        "this cell has row style"
                    }
                    cell {
                        "this cell has row style"
                    }
                    cell(style: 'customStyle') {
                        "this cell has common style and custom style"
                    }
                }
            }
        }

        then:
        workbook.getSheet("Demo styles") != null
        workbook.getSheet("Demo styles").getRow(0) != null
        workbook.getSheet("Demo styles").getRow(0).getCell(0).getCellStyle().alignment == CellStyle.ALIGN_CENTER
        workbook.getSheet("Demo styles").getRow(0).getCell(0).getCellStyle().borderBottom == CellStyle.BORDER_DASH_DOT
        workbook.getSheet("Demo styles").getRow(0).getCell(1).cellStyle.alignment == CellStyle.ALIGN_CENTER
        workbook.getSheet("Demo styles").getRow(0).getCell(1).cellStyle.borderBottom == CellStyle.BORDER_DASH_DOT
        workbook.getSheet("Demo styles").getRow(0).getCell(2).cellStyle.alignment == CellStyle.ALIGN_LEFT
        workbook.getSheet("Demo styles").getRow(0).getCell(2).cellStyle.borderTop == CellStyle.BORDER_DOUBLE
    }

    def "test fonts"() {
        setup:
        def builder = new ExcelBuilder()

        when:
        builder.config {
            font('waterfall configured font') { font ->
                font.fontName = 'Arial'
            }
            style('style with waterfall configured font') { cellStyle ->
                cellStyle.font = font('waterfall configured font')
                cellStyle
            }
            style('style with included configured font') { cellStyle ->
                cellStyle.font = font('included configured font') { font ->
                    font.fontName = 'Arial'
                }
            }
        }
        def workbook = builder.build {
            sheet {
                row {
                    cell(style: 'style with waterfall configured font') {
                        'test waterfall configured font'
                    }
                    cell(style: 'style with included configured font') {
                        'test included configured font'
                    }
                }
            }
        }

        then:
        workbook.getSheetAt(0) != null
        workbook.getSheetAt(0).getRow(0) != null
        workbook.getSheetAt(0).getRow(0).getCell(0) != null
        workbook.getSheetAt(0).getRow(0).getCell(0).stringCellValue == 'test waterfall configured font'
        (workbook.getSheetAt(0).getRow(0).getCell(0).cellStyle as XSSFCellStyle).font.fontName == 'Arial'
        workbook.getSheetAt(0).getRow(0).getCell(1) != null
        workbook.getSheetAt(0).getRow(0).getCell(1).stringCellValue == 'test included configured font'
        (workbook.getSheetAt(0).getRow(0).getCell(1).cellStyle as XSSFCellStyle).font.fontName == 'Arial'
    }

    def "test formats"() {
        setup:
        def builder = new ExcelBuilder()

        when:
        builder.config {
            // style with waterfall configured format (string)
            dataFormat('1', '# ##0.00')
            style('1') { cellStyle ->
                cellStyle.dataFormat = dataFormat('1')
            }

            // style with included configured format (string)
            style('2') { cellStyle ->
                cellStyle.dataFormat = dataFormat('2', '# ### ##0.00')
            }

            // style with waterfall configured format (closure)
            dataFormat('3') { format ->
                '# ### ### ##0.00'
            }
            style('3') { cellStyle ->
                cellStyle.dataFormat = dataFormat('3')
            }

            // style with included configured format (closure)
            style('4') { XSSFCellStyle cellStyle ->
                cellStyle.dataFormat = dataFormat('4') { format ->
                    '# ### ### ### ##0.00'
                }
            }
        }
        def workbook = builder.build {
            sheet {
                row {
                    cell(style: '1') {
                        'test style with waterfall configured format (string)'
                    }
                    cell(style: '2') {
                        'test style with included configured format (string)'
                    }
                    cell(style: '3') {
                        'test style with waterfall configured format (closure)'
                    }
                    cell(style: '4') {
                        'test style with included configured format (closure)'
                    }
                }
            }
        }

        then:
        workbook.getSheetAt(0) != null
        workbook.getSheetAt(0).getRow(0) != null
        workbook.getSheetAt(0).getRow(0).getCell(0) != null
        workbook.getSheetAt(0).getRow(0).getCell(0).cellStyle.dataFormatString == '# ##0.00'
        workbook.getSheetAt(0).getRow(0).getCell(1) != null
        workbook.getSheetAt(0).getRow(0).getCell(1).cellStyle.dataFormatString == '# ### ##0.00'
        workbook.getSheetAt(0).getRow(0).getCell(2) != null
        workbook.getSheetAt(0).getRow(0).getCell(2).cellStyle.dataFormatString == '# ### ### ##0.00'
        workbook.getSheetAt(0).getRow(0).getCell(3) != null
        workbook.getSheetAt(0).getRow(0).getCell(3).cellStyle.dataFormatString == '# ### ### ### ##0.00'
    }

    def "test spans"() {
        setup:
        def builder = new ExcelBuilder()

        when:
        def workbook = builder.build {
            sheet(name: "Demo spans") {
                row {
                    // A1:B1
                    cell(colspan: 2) {
                        "cell has width 2 columns"
                    }
                    cell()
                    // C1:C2
                    cell(rowspan: 2, style: 'wrap', width: 12) {
                        "cell has height 2 rows"
                    }
                    // D1:E2
                    cell(colspan: 2, rowspan: 2, style: 'wrap') {
                        "cell has heigth 2 rows and width 2 columns"
                    }
                }
                // this row not necessary, this row show only dummy-row
                row()
            }
        }

        then:
        workbook.getSheet("Demo spans") != null
        workbook.getSheet("Demo spans").getRow(0) != null
        workbook.getSheet("Demo spans").getRow(0).getCell(0) != null
        workbook.getSheet("Demo spans").getRow(0).getCell(1) != null
        workbook.getSheet("Demo spans").getRow(0).getCell(2) != null
        workbook.getSheet("Demo spans").getRow(0).getCell(3) != null
        workbook.getSheet("Demo spans").getRow(0).getCell(4) != null
        workbook.getSheet("Demo spans").getRow(0).getCell(5) == null
        workbook.getSheet("Demo spans").getRow(1) != null
        workbook.getSheet("Demo spans").getRow(0).getCell(0) != null
        workbook.getSheet("Demo spans").getMergedRegion(0) != null
        workbook.getSheet("Demo spans").getMergedRegion(0).formatAsString() == "A1:B1"
        workbook.getSheet("Demo spans").getMergedRegion(1) != null
        workbook.getSheet("Demo spans").getMergedRegion(1).formatAsString() == "C1:C2"
        workbook.getSheet("Demo spans").getMergedRegion(2) != null
        workbook.getSheet("Demo spans").getMergedRegion(2).formatAsString() == "D1:E2"
    }

    def "test config height and width"() {
        setup:
        def builder = new ExcelBuilder()

        when:
        def workbook = builder.build {
            sheet(name: "Demo config height and width", widthColumns: ['default', 25, 30]) {
                row(height: 10) {
                    cell {
                        "this row has height 30 and default width"
                    }
                }
                row {
                    cell()
                    cell {
                        "this column has width 30"
                    }
                    cell {
                        "this column has width 35"
                    }
                }
                row(height: defaultRowHeight) {
                    cell()
                }
            }
        }

        then:
        workbook.getSheet("Demo config height and width") != null
        workbook.getSheet("Demo config height and width").getColumnWidth(0) / 256 as Integer == workbook.getSheet("Demo config height and width").defaultColumnWidth
        workbook.getSheet("Demo config height and width").getColumnWidth(1) / 256 as Integer == 25
        workbook.getSheet("Demo config height and width").getColumnWidth(2) / 256 as Integer == 30
        workbook.getSheet("Demo config height and width").getRow(0) != null
        workbook.getSheet("Demo config height and width").getRow(0).height / 20 as Integer == 10
        workbook.getSheet("Demo config height and width").getRow(0).getCell(0) != null
        workbook.getSheet("Demo config height and width").getRow(0).getCell(0) != null
        workbook.getSheet("Demo config height and width").getRow(1) != null
        workbook.getSheet("Demo config height and width").getRow(1).height == workbook.getSheet("Demo config height and width").defaultRowHeight
        workbook.getSheet("Demo config height and width").getRow(1).getCell(0) != null
        workbook.getSheet("Demo config height and width").getRow(1).getCell(1) != null
        workbook.getSheet("Demo config height and width").getRow(1).getCell(2) != null
        workbook.getSheetAt(0).getRow(2) != null
        workbook.getSheetAt(0).getRow(2).height == workbook.getSheetAt(0).defaultRowHeight
    }

    def "test dynamic data"() {
        setup:
        def builder = new ExcelBuilder()

        when:
        def workbook = builder.build {
            sheet(name: "Demo dynamic data", widthColumns: [12, 12]) {
                def data1 = [
                        ["value 1", "value 2"],
                        ["value 3", "value 4"],
                        ["value 5", "value 6"]
                ]

                for (def rowData : data1) {
                    row {
                        for (def cellData : rowData) {
                            cell {
                                cellData
                            }
                        }
                    }
                }

                row()

                def data2 = [
                        [prop1: "row 1 value 1", prop2: "row 1 value 2"],
                        [prop1: "row 2 value 1", prop2: "row 2 value 2"],
                        [prop1: "row 3 value 1", prop2: "row 3 value 2"],
                        // For this data row will create merge region A8:B8
                        [prop2: "row 4 value 2"]
                ]

                for (def rowData : data2) {
                    row {
                        cell(colspan: rowData.prop1 ? 1 : 2) {
                            rowData.prop2
                        }
                        if (rowData.prop1) {
                            cell {
                                rowData.prop1
                            }
                        }
                    }
                }
            }
        }

        then:
        workbook.getSheet("Demo dynamic data") != null
        workbook.getSheet("Demo dynamic data").getRow(0) != null
        workbook.getSheet("Demo dynamic data").getRow(0).getCell(0) != null
        workbook.getSheet("Demo dynamic data").getRow(0).getCell(0).stringCellValue == "value 1"
        workbook.getSheet("Demo dynamic data").getRow(0).getCell(1) != null
        workbook.getSheet("Demo dynamic data").getRow(0).getCell(1).stringCellValue == "value 2"
        workbook.getSheet("Demo dynamic data").getRow(1) != null
        workbook.getSheet("Demo dynamic data").getRow(1).getCell(0) != null
        workbook.getSheet("Demo dynamic data").getRow(1).getCell(0).stringCellValue == "value 3"
        workbook.getSheet("Demo dynamic data").getRow(1).getCell(1) != null
        workbook.getSheet("Demo dynamic data").getRow(1).getCell(1).stringCellValue == "value 4"
        workbook.getSheet("Demo dynamic data").getRow(2) != null
        workbook.getSheet("Demo dynamic data").getRow(2).getCell(0) != null
        workbook.getSheet("Demo dynamic data").getRow(2).getCell(0).stringCellValue == "value 5"
        workbook.getSheet("Demo dynamic data").getRow(2).getCell(1) != null
        workbook.getSheet("Demo dynamic data").getRow(2).getCell(1).stringCellValue == "value 6"
        workbook.getSheet("Demo dynamic data").getRow(3) != null
        workbook.getSheet("Demo dynamic data").getRow(4) != null
        workbook.getSheet("Demo dynamic data").getRow(4).getCell(0) != null
        workbook.getSheet("Demo dynamic data").getRow(4).getCell(0).stringCellValue == "row 1 value 2"
        workbook.getSheet("Demo dynamic data").getRow(4).getCell(1) != null
        workbook.getSheet("Demo dynamic data").getRow(4).getCell(1).stringCellValue == "row 1 value 1"
        workbook.getSheet("Demo dynamic data").getRow(5) != null
        workbook.getSheet("Demo dynamic data").getRow(5).getCell(0) != null
        workbook.getSheet("Demo dynamic data").getRow(5).getCell(0).stringCellValue == "row 2 value 2"
        workbook.getSheet("Demo dynamic data").getRow(5).getCell(1) != null
        workbook.getSheet("Demo dynamic data").getRow(5).getCell(1).stringCellValue == "row 2 value 1"
        workbook.getSheet("Demo dynamic data").getRow(6) != null
        workbook.getSheet("Demo dynamic data").getRow(6).getCell(0) != null
        workbook.getSheet("Demo dynamic data").getRow(6).getCell(0).stringCellValue == "row 3 value 2"
        workbook.getSheet("Demo dynamic data").getRow(6).getCell(1) != null
        workbook.getSheet("Demo dynamic data").getRow(6).getCell(1).stringCellValue == "row 3 value 1"
        workbook.getSheet("Demo dynamic data").getRow(7) != null
        workbook.getSheet("Demo dynamic data").getRow(7).getCell(0) != null
        workbook.getSheet("Demo dynamic data").getRow(7).getCell(0).stringCellValue == "row 4 value 2"
        workbook.getSheet("Demo dynamic data").getMergedRegion(0) != null
        workbook.getSheet("Demo dynamic data").getMergedRegion(0).formatAsString() == "A8:B8"
    }

    def "test several sheets"() {
        setup:
        def builder = new ExcelBuilder()

        when:
        def workbook = builder.build {
            sheet() {
            }
            sheet() {
            }
            sheet() {
            }
        }

        then:
        workbook.getSheetAt(0) != null
        workbook.getSheetAt(1) != null
        workbook.getSheetAt(2) != null
    }

    def "test skipping"() {
        setup:
        def builder = new ExcelBuilder()

        when:
        def workbook = builder.build {
            sheet {
                row {
                    cell {
                        'A'
                    }
                    skipCell()
                    cell {
                        'B'
                    }
                    skipCell(2)
                    cell {
                        'C'
                    }
                }
                skipRow()
                row {
                    cell {
                        'D'
                    }
                }
                skipRow(2)
                row {
                    cell {
                        'E'
                    }
                }
            }
        }

        then:
        workbook.getSheetAt(0) != null
        workbook.getSheetAt(0).getRow(0) != null
        workbook.getSheetAt(0).getRow(0).getCell(0) != null
        workbook.getSheetAt(0).getRow(0).getCell(0).stringCellValue == 'A'
        workbook.getSheetAt(0).getRow(0).getCell(1) != null
        workbook.getSheetAt(0).getRow(0).getCell(2) != null
        workbook.getSheetAt(0).getRow(0).getCell(2).stringCellValue == 'B'
        workbook.getSheetAt(0).getRow(0).getCell(3) != null
        workbook.getSheetAt(0).getRow(0).getCell(4) != null
        workbook.getSheetAt(0).getRow(0).getCell(5) != null
        workbook.getSheetAt(0).getRow(0).getCell(5).stringCellValue == 'C'
        workbook.getSheetAt(0).getRow(1) != null
        workbook.getSheetAt(0).getRow(2) != null
        workbook.getSheetAt(0).getRow(2).getCell(0) != null
        workbook.getSheetAt(0).getRow(2).getCell(0).stringCellValue == 'D'
        workbook.getSheetAt(0).getRow(3) != null
        workbook.getSheetAt(0).getRow(4) != null
        workbook.getSheetAt(0).getRow(5) != null
        workbook.getSheetAt(0).getRow(5).getCell(0) != null
        workbook.getSheetAt(0).getRow(5).getCell(0).stringCellValue == 'E'
    }

}
