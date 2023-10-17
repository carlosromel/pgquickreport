/**
 * Copyright (C) 2017, 2023 Carlos Romel Pereira da Silva, carlos.romel@gmail.com
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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author Carlos Romel Pereira da Silva, carlos.romel@gmail.com
 */
public class Main {

    public static void main(String[] args) {
        PGQuickReport qr = new PGQuickReport();
        String propertyFile = "pgquickreport.properties";

        if (args.length > 0) {
            if (new File(propertyFile).exists()) {
                String url = "jdbc:postgresql://localhost:5432/template1";
                String user = "postgres";
                String pass = "";

                try {
                    Properties prop = new Properties();
                    prop.load(new FileReader(propertyFile));
                    url = prop.getProperty("url", url);
                    user = prop.getProperty("user", user);
                    pass = prop.getProperty("pass", pass);
                    Connection con = DriverManager.getConnection(url, user, pass);

                    for (String arg : args) {
                        if (new File(arg).exists()) {
                            String title = getTitle(arg);
                            qr.process(con, title, new File(arg));
                        } else {
                            System.out.printf("O arquivo %s não existe.", arg);
                        }
                    }
                } catch (FileNotFoundException ex) {
                    System.out.printf("O arquivo %s não foi encontrado.%n", propertyFile, ex);
                } catch (IOException ex) {
                    System.out.printf("O arquivo %s não pode ser lido.%n", propertyFile, ex);
                } catch (SQLException ex) {
                    System.out.printf("A conexão %s não pode ser estabelecida.%n", url, ex);
                }
            } else {
                System.out.printf("O arquivo %s não foi encontrado no diretório atual.", propertyFile);
            }
        } else {
            System.out.println("Informe um arquivo de consulta (.sql)");
            System.out.println("Ex.:java -jar pgquickreport.jar arquivo1.sql [arquivo2.sql] [arquivoN.sql]");
        }
    }

    /**
     * Retorna o título da consulta.
     *
     * @param queryFileName Arquivo que contém a consulta.
     *
     * @return Título do relatório.
     */
    private static String getTitle(String queryFileName) throws IOException {
        String title = queryFileName;
        Path queryFIle = new File(queryFileName).toPath();
        List<String> lines = Files.readAllLines(queryFIle);

        for (int l = 0; l < lines.size(); ++l) {
            String line = lines.get(l);

            if (line.startsWith("/**")) {
                for (int l2 = l + 1; l2 < lines.size(); ++l2) {
                    line = lines.get(l2);
                    if (line.startsWith(" * ")) {
                        title = line.substring(3);
                        break;
                    }
                }
                break;
            }
        }

        return title;
    }
}
