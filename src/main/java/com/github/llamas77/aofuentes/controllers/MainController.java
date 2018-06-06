package com.github.llamas77.aofuentes.controllers;

import com.github.llamas77.aofuentes.Main;
import com.github.llamas77.aofuentes.modules.Csv;
import com.github.llamas77.aofuentes.modules.Font;
import com.github.llamas77.aofuentes.modules.Fonts;
import com.github.llamas77.aofuentes.modules.Util;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTextField;
import com.opencsv.CSVReader;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    private JFXListView<Font> lstFuentes;

    @FXML
    private JFXTextField txtTextura;

    @FXML
    private JFXTextField txtOffset;

    @FXML
    private JFXTextField txtUbicacion;

    @FXML
    private JFXButton btnExaminar;

    @FXML
    private JFXButton btnGuardar;

    @FXML
    private JFXButton btnBorrar;


    @FXML
    private JFXButton btnCancelar;

    private Font seleccionada;


    private Fonts fonts;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fonts = new Fonts();

        lstFuentes.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            seleccionada = newValue;
            if (seleccionada != null) {
                txtOffset.setText(seleccionada.getOffset() + "");
                txtTextura.setText(seleccionada.getTex() + "");
                btnExaminar.disableProperty().setValue(true);
                txtUbicacion.disableProperty().setValue(true);
                btnGuardar.setText("Guardar");
            }
        });

        refreshFonts();
    }

    /**
     * Actualiza el listado de fuentes
     */
    private void refreshFonts() {
        lstFuentes.itemsProperty().getValue().clear();
        int cantFonts = fonts.getFont().size();
        for (int i = 0; i < cantFonts; i++) {
            lstFuentes.itemsProperty().getValue().add(fonts.getFont().get(i));
        }
    }

    @FXML
    void borrar(MouseEvent event) {
        if (seleccionada == null) return;
        fonts.getFont().remove(seleccionada);
        save();
        cancelar(null);
        refreshFonts();
    }

    @FXML
    void cancelar(MouseEvent event) {
        btnGuardar.setText("Agregar");
        btnExaminar.disableProperty().setValue(false);
        txtUbicacion.disableProperty().setValue(false);
        txtUbicacion.setText("");
        txtTextura.setText("");
        txtOffset.setText("");

        lstFuentes.getSelectionModel().clearSelection();
    }

    @FXML
    void examinar(MouseEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Buscar archivo CSV");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));

        // Agregar filtros para facilitar la busqueda
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV", "*.csv")
        );

        // Obtener la imagen seleccionada
        File f = fileChooser.showOpenDialog(Main.instance.stage);
        if (f != null)
            txtUbicacion.setText(f.getAbsolutePath());
    }

    @FXML
    void guardar(MouseEvent event) {


        // Validaciones del formulario
        if (seleccionada == null && txtUbicacion.getText().trim().equals("")) {
            Alert a = new Alert(Alert.AlertType.INFORMATION, "Debe seleccionar un archivo CSV primero.", ButtonType.OK);
            a.showAndWait();
            return;
        }

        if (txtTextura.getText().trim().equals("") || txtOffset.getText().trim().equals("")) {
            Alert a = new Alert(Alert.AlertType.INFORMATION, "Alguno de los campos está incompleto.", ButtonType.OK);
            a.showAndWait();
            return;
        }

        if (seleccionada != null) {
            seleccionada.setTex(Integer.parseInt(txtTextura.getText()));
            seleccionada.setOffset(Integer.parseInt(txtOffset.getText()));
        }
        else {
            // Leo el CSV y guardo su info en un array
            ArrayList<String> array = new ArrayList<>();
            try (CSVReader reader = new CSVReader(new FileReader(txtUbicacion.getText()))) {
                String[] nextLine;
                while ((nextLine = reader.readNext()) != null) {
                    array.add(nextLine[1]);
                }
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }

            // INTERPRETAR CSV DESDE EL ARRAY
            Csv csv = new Csv();
            csv.imageWidth = Integer.parseInt(array.get(0));
            csv.imageHeight = Integer.parseInt(array.get(1));
            csv.cellWidth = Integer.parseInt(array.get(2));
            csv.cellHeight = Integer.parseInt(array.get(3));
            csv.startChar = Integer.parseInt(array.get(4));
            csv.name = array.get(5) + ", " + array.get(6); // Combinacion de nombre + tamaño
            for (int i = 8; i < 264; i++) {
                csv.baseWidth[i - 8] = Integer.parseInt(array.get(i));
            }
            csv.globalWOffset = Integer.parseInt(array.get(1032));

            // CONVERSIÓN AL SISTEMA DE FUENTES DEL AO
            fonts.getFont().add(new Font());
            int index = fonts.getFont().size() - 1;

            fonts.getFont().get(index).setName(csv.name);
            fonts.getFont().get(index).setTex(Integer.parseInt(txtTextura.getText()));
            fonts.getFont().get(index).setOffset(Integer.parseInt(txtOffset.getText()));

            // Indicar la informacion de cada char existente
            int charsPerRow = csv.imageWidth / csv.cellWidth;
            int quantRows = (256 - csv.startChar) / charsPerRow;
            int quantLastRow = (256 - csv.startChar) % charsPerRow;

            int tempX = 0, tempY = 0;
            int startChar = csv.startChar;
            for (int i = 1; i <= quantRows; i++) {
                for (int j = startChar; j - startChar < charsPerRow; j++) {
                    fonts.getFont().get(index).getChar()[j].setSrcX(tempX);
                    fonts.getFont().get(index).getChar()[j].setSrcY(tempY);
                    fonts.getFont().get(index).getChar()[j].setSrcWidth(csv.baseWidth[j] + csv.globalWOffset);
                    fonts.getFont().get(index).getChar()[j].setSrcHeight(csv.cellHeight);
                    tempX += csv.cellWidth;
                }
                tempX = 0;
                tempY = i * csv.cellHeight;
                startChar += charsPerRow;
            }

            for (int i = startChar; i - startChar < quantLastRow; i++) {
                fonts.getFont().get(index).getChar()[i].setSrcX(tempX);
                fonts.getFont().get(index).getChar()[i].setSrcY(tempY);
                fonts.getFont().get(index).getChar()[i].setSrcWidth(csv.baseWidth[i] + csv.globalWOffset);
                fonts.getFont().get(index).getChar()[i].setSrcHeight(csv.cellHeight);
                tempX += csv.cellWidth;
            }
        }

        save();
        refreshFonts();

        cancelar(null);
    }

    private void save() {
        // GUARDAR TODAS LAS FUENTES EN EL ARCHIVO
        try {
            RandomAccessFile file = new RandomAccessFile("fonts.ind", "rw");
            file.writeInt(Util.bigToLittle_Int(fonts.getFont().size()));
            for (int i = 0; i < fonts.getFont().size(); i++) {
                //file.writeUTF(fonts.getFont().get(i).getName());
                file.writeInt(Util.bigToLittle_Int(fonts.getFont().get(i).getTex()));
                file.writeInt(Util.bigToLittle_Int(fonts.getFont().get(i).getOffset()));
                for (int j = 0; j < fonts.getFont().get(i).getChar().length; j++) {
                    file.writeInt(Util.bigToLittle_Int(fonts.getFont().get(i).getChar()[j].getSrcX()));
                    file.writeInt(Util.bigToLittle_Int(fonts.getFont().get(i).getChar()[j].getSrcY()));
                    file.writeInt(Util.bigToLittle_Int(fonts.getFont().get(i).getChar()[j].getSrcWidth()));
                    file.writeInt(Util.bigToLittle_Int(fonts.getFont().get(i).getChar()[j].getSrcHeight()));
                }
            }
            file.close();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}