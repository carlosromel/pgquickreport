/**
 * Copyright (C) 2017 Carlos Romel Pereira da Silva, carlos.romel@gmail.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package pgquickreport;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Carlos Romel Pereira da Silva, carlos.romel@gmail.com
 */
public class PGQuickReport {

    private static final String COLUMN_HEADER_MODEL
                                = "%n"
                                  + "                        <th class=\"ReportTableHeaderCell\" width=\"2,857142857142857%%\">%s</th>";
    private static final String COLUMN_MODEL
                                = "                        <td class=\"ReportTableValueCell\">%s</td>\n";
    private static final String ROW_MODEL
                                = ""
                                  + "<tr class=\"%s\">\n"
                                  + "%s"
                                  + "                    </tr>%n                    ";

    /**
     * Gera um relatório no formato QuickReport, do PGAdmin III.
     *
     * @param con             Conexão utilizada.
     * @param consultFileName Nome do arquivo de consulta.
     *
     * @return Nome do arquivo de relatório.
     */
    public String process(Connection con, String consultFileName) {

        return process(con, new File(consultFileName));
    }

    /**
     * Gera um relatório no formato QuickReport, do PGAdmin III.
     *
     * @param con         Conexão utilizada.
     * @param consultFile Arquivo de consulta.
     *
     * @return Nome do arquivo de relatório.
     */
    public String process(Connection con, File consultFile) {
        String reportName = changeExtension(consultFile, "html");

        try {
            String sql = getFileContent(consultFile);
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            ResultSetMetaData metaData = rs.getMetaData();
            StringBuilder columnHeaders = new StringBuilder();

            for (int c = 1; c <= metaData.getColumnCount(); ++c) {
                columnHeaders.append(String.format(COLUMN_HEADER_MODEL, metaData.getColumnName(c)));
            }

            int rowCount = 0;
            StringBuilder resultRows = new StringBuilder();
            while (rs.next()) {
                String parity = String.format("ReportDetails%sDataRow", (rowCount++ % 2) == 0 ? "Even" : "Odd");
                StringBuilder columnResult = new StringBuilder();

                for (int c = 1; c <= metaData.getColumnCount(); ++c) {
                    columnResult.append(String.format(COLUMN_MODEL, rs.getString(c)));
                }

                resultRows.append(String.format(ROW_MODEL, parity, columnResult));
            }

            String url = con.getMetaData().getURL();

            String title = "Título";
            String generated = new SimpleDateFormat("E d MMM Y hh:mm:ss Z").format(new Date());
            String database = url.split("/")[3];
            String user = con.getMetaData().getUserName();
            String server = url.split("/")[2].split(":")[0];
            String port = url.split("/")[2].split(":")[1];
            String numRows = String.format("%d", rowCount);
            String numCols = String.format("%d", metaData.getColumnCount());
            String model = getFileContent("model.html");
            String msg = model
                    .replace("{0}", title)
                    .replace("{1}", generated)
                    .replace("{2}", database)
                    .replace("{3}", user)
                    .replace("{4}", server)
                    .replace("{5}", port)
                    .replace("{6}", columnHeaders)
                    .replace("{7}", resultRows)
                    .replace("{8}", numRows)
                    .replace("{9}", numCols)
                    .replace("{10}", sql);

            Files.write(new File(reportName).toPath(), msg.getBytes());
        } catch (IOException ex) {
            System.out.println(String.format("O arquivo %s não pode ser gravado.", reportName, ex));
        } catch (SQLException ex) {
            System.out.println(String.format("A consulta %s não pode ser executada.", consultFile, ex));
        }

        return reportName;
    }

    private String getFileContent(String consultFile) throws IOException {

        return getFileContent(new File(consultFile));
    }

    /**
     * Retorna o conteúdo de um determinado arquivo.
     *
     * @param consultFile Arquivo.
     *
     * @return Conteúdo do arquivo.
     *
     * @throws IOException
     */
    private String getFileContent(File consultFile) throws IOException {
        final Path path = consultFile.toPath();
        final byte[] bytes = Files.readAllBytes(path);
        final String buffer = new String(bytes);

        return buffer;
    }

    /**
     * Altera a extensão de um arquivo.
     *
     * @param file      Arquivo a ser alterado.
     * @param extension Nova extensão.
     *
     * @return Nome do arquivo, com a extensão alterada.
     */
    private String changeExtension(File file, String extension) {
        String newFileName = file.getName();

        if (newFileName.contains(".")) {
            newFileName = newFileName.substring(0, newFileName.lastIndexOf("."));
        }

        return newFileName + "." + extension;
    }
}
