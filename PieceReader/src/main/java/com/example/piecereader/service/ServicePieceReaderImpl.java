package com.example.piecereader.service;

import com.example.piecereader.model.*;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import nu.pattern.OpenCV;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ServicePieceReaderImpl implements ServicePieceReader{
    @Override
    public DocumentNumerised convert(TypeDocument typeDocument, byte[] image) throws Exception {
        switch (typeDocument) {
            case PERMIS_RECTO:
                return readPermisRecto(cropAndAdjustPermis(image));
            case PERMIS_VERSO :
                return readPermisVerso(cropAndAdjust(image,150));
            case CARTE_GRISE:
                return readCarteGrise(cropAndAdjust(image,190));
            case CNI :
                return readCNIRecto(cropAndAdjustCNI(image, 150));
            default:
                throw new Exception();
        }
    }

    private Mat cropAndAdjust(byte[] imageSrc,int thresh){
        // Charger la bibliothèque OpenCV
        OpenCV.loadLocally();

        // Charger l'image
        Mat image = Imgcodecs.imdecode(new MatOfByte(imageSrc),Imgcodecs.IMREAD_COLOR);

        //Recadrer l'image dynamiquement
        // Convertir en niveaux de gris et appliquer un seuillage
        Mat gray = new Mat();
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(gray, gray, thresh, 255, Imgproc.THRESH_BINARY_INV);

        // Détecter les contours
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(gray, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Filtrer les contours et trouver le plus grand
        double maxArea = 0;
        MatOfPoint maxContour = new MatOfPoint();
        for (MatOfPoint contour : contours) {
            double area = Imgproc.contourArea(contour);
            if (area > maxArea && area > 1000) { // Vérifie aussi que le contour est suffisamment grand
                maxArea = area;
                maxContour = contour;
            }
        }

        // Vérifier si un contour significatif a été trouvé
        Mat croppedImage = new Mat();
        if (maxArea > 0) {
            // Recadrer l'image
            Rect rect = Imgproc.boundingRect(maxContour);
            croppedImage = new Mat(image, rect);
        }

        // Convertir l'image en noir et blanc
        Mat grayscaleImage = new Mat();
      //  Imgproc.adaptiveThreshold(croppedImage,grayscaleImage,255,Imgproc.ADAPTIVE_THRESH_MEAN_C,Imgproc.THRESH_BINARY,15,10);
        Imgproc.cvtColor(croppedImage, grayscaleImage, Imgproc.COLOR_BGR2GRAY);

        // Passer l'exposition à 100%
        Mat adjustedImage = new Mat();
        grayscaleImage.convertTo(adjustedImage, -1, 2, 0); // L'exposition est multipliée par 2
        return adjustedImage;
    }


    private Mat cropAndAdjustPermis(byte[] imageSrc){
        // Charger la bibliothèque OpenCV
        OpenCV.loadLocally();

        Mat image = Imgcodecs.imdecode(new MatOfByte(imageSrc),Imgcodecs.IMREAD_COLOR);

        //Recadrer l'image dynamiquement
        // Convertir en niveaux de gris et appliquer un seuillage
        Mat gray = new Mat();
        Imgproc.cvtColor(image, gray,6);
        Imgproc.threshold(gray, gray, 150, 255, Imgproc.THRESH_BINARY_INV);

        // Détecter les contours
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(gray, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Filtrer les contours et trouver le plus grand
        double maxArea = 0;
        MatOfPoint maxContour = new MatOfPoint();
        for (MatOfPoint contour : contours) {
            double area = Imgproc.contourArea(contour);
            if (area > maxArea && area > 1000) { // Vérifie aussi que le contour est suffisamment grand
                maxArea = area;
                maxContour = contour;
            }
        }

        // Vérifier si un contour significatif a été trouvé
        Mat croppedImageDeb = new Mat();
        if (maxArea > 0) {
            // Recadrer l'image
            Rect rect = Imgproc.boundingRect(maxContour);
            croppedImageDeb = new Mat(image, rect);
        }

        // Récupérer les dimensions de l'image
        int width = croppedImageDeb.cols();
        int height = croppedImageDeb.rows();

        Rect cropRect = new Rect(660, 0, width - 1320, height);

        // Recadrer l'image
        Mat croppedImage = new Mat(croppedImageDeb, cropRect);

        // Convertir l'image en noir et blanc
        Mat grayscaleImage = new Mat();
        Imgproc.cvtColor(croppedImage, grayscaleImage, Imgproc.COLOR_BGR2GRAY);

        // Passer l'exposition à 100%
        Mat adjustedImage = new Mat();
        grayscaleImage.convertTo(adjustedImage, -1, 3, 0); // L'exposition est multipliée par 2

        //Application de Tesseract à l'image traitée
        return adjustedImage;
    }

    private PermisRecto readPermisRecto(Mat imageAjust) throws Exception {

        ITesseract instance = new Tesseract();
        instance.setDatapath("/usr/share/tesseract-ocr/4.00/tessdata");

        try {
            String ocrResultRecto = instance.doOCR(matToBufferedImage(imageAjust));
            System.out.println(ocrResultRecto);
            // Traitement et affichage des données extraites
            return parseAndPrintDataRecto(ocrResultRecto);
        } catch (TesseractException e) {
            throw new Exception(e.getMessage());
        }
    }

    private PermisVerso readPermisVerso(Mat imageAjust) throws Exception {

        ITesseract instance = new Tesseract();
        instance.setDatapath("/usr/share/tesseract-ocr/4.00/tessdata");

        try {
            String ocrResultRecto = instance.doOCR(matToBufferedImage(imageAjust));
            System.out.println("OCR Result:\n" + ocrResultRecto);
            // Traitement et affichage des données extraites
            return parseAndPrintDataVerso(ocrResultRecto);
        } catch (TesseractException e) {
            throw new Exception(e.getMessage());
        }
    }

    private static PermisRecto parseAndPrintDataRecto(String data) {
        // Normalisation en gardant les espaces
        String normalizedData = data.toUpperCase().replaceAll("[^A-Z0-9. ]", "");
        String lastName = extractAndAdjustLastName(data);
        String firstName = extractEtAjustePrenom(data);
        System.out.println("Nom retourvé " + lastName);
        System.out.println("Prenom retourvé " + firstName);

        //String firstName = extractData(normalizedData, "2(.*?)3");
        //String[] prenom = firstName.split(" ");
        //firstName = prenom[0];
        String expiryDate = extractDate(data);
        //String expiryDate = extractData(normalizedData, "4B(.*?)4C");

        return new PermisRecto(lastName,firstName,expiryDate);
    }

    public static String extractEtAjustePrenom(String data) {
        String[] lines = data.split("\n");

        // Vérifiez si assez de lignes détectée
        if (lines.length < 5) {
            return "pas trouve";
        }

        String fifthLine = lines[4];//.replaceAll("[^A-Za-z ]", ""); // on enleve ce qui n'est pas azAZ
        String[] parts = fifthLine.split("\\s+");

        if (parts.length > 1) {
            String secondWord = parts[1];
            if (secondWord.length() > 0) {
                secondWord = secondWord.substring(1);
                System.out.println(secondWord);
                return secondWord;
            }
        }

        return "pas trouve";
    }

    public static String extractAndAdjustLastName(String data) {
        String[] lines = data.split("\n");
        System.out.println("lines : \n"+lines[0]);
        // Vérifiez s'il y a suffisamment de lignes
        if (lines.length < 3) {
            return "pas trouve";
        }

        String thirdLine = lines[2].replaceAll("[^A-Za-z]", "");

        // Enlever le premier et le dernier caractère
        if (thirdLine.length() > 2) {
            thirdLine = thirdLine.substring(1, thirdLine.length() - 1);
            return thirdLine;
        }

        return "pas trouve";
    }

    private static String extractData(String data, String pattern) {
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(data);

        if (m.find()) {
            return m.group(1).trim();
        }

        return "Not found";
    }

    private static String extractDate(String data) {
        // Cette expression régulière recherche une date avec des slashs ou des points comme séparateurs,
        // et une année comprise entre 2028 et 2050
        String datePattern = "(\\b[0-3]?[0-9][/.]?[0-1]?[0-9][/.]?(2028|2029|20[3-4][0-9]|2050)\\b)";
        Pattern r = Pattern.compile(datePattern);
        Matcher m = r.matcher(data);

        if (m.find()) {
            return m.group(1);
        }

        return "Date non trouvée";
    }

    private static PermisVerso parseAndPrintDataVerso(String data) {
        String normalizedData = data.toUpperCase().replaceAll("[^A-Z0-9. ]", "");

        String numero = extractAndConcatenateNumbers(data);
        System.out.println(numero);
        return new PermisVerso(numero);


    }

    private static String extractAndConcatenateNumbers(String data) {
        // Cherche toutes les séries de chiffres
        String pattern = "\\b\\d{4,6}\\b";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(data);

        String firstPart = "";
        String secondPart = "";

        while (m.find()) {
            if (m.group().length() == 6) {
                if (firstPart.isEmpty()) {
                    firstPart = m.group();
                } else {
                    secondPart = m.group();
                    return firstPart + secondPart;
                }
            }
        }

        // Recherche une deuxième fois pour les séries de 5 et 4 chiffres si les séries de 6 chiffres ne sont pas trouvées
        m.reset();
        while (m.find()) {
            if (m.group().length() == 5 && firstPart.isEmpty()) {
                firstPart = m.group();
            } else if (m.group().length() == 4 && !firstPart.isEmpty()) {
                secondPart = m.group();
                return firstPart + secondPart;
            }
        }

        return "Numéros non trouvés";
    }

    // Function to convert Mat to BufferedImage
    public static BufferedImage matToBufferedImage(Mat matrix) {
        int cols = matrix.cols();
        int rows = matrix.rows();
        int elemSize = (int) matrix.elemSize();
        byte[] data = new byte[cols * rows * elemSize];
        int type;
        matrix.get(0, 0, data);

        switch (matrix.channels()) {
            case 1:
                type = BufferedImage.TYPE_BYTE_GRAY;
                break;
            case 3:
                type = BufferedImage.TYPE_3BYTE_BGR;
                // bgr to rgb
                byte b;
                for(int i = 0; i < data.length; i = i + 3) {
                    b = data[i];
                    data[i] = data[i + 2];
                    data[i + 2] = b;
                }
                break;
            default:
                return null;
        }

        BufferedImage image2 = new BufferedImage(cols, rows, type);
        image2.getRaster().setDataElements(0, 0, cols, rows, data);

        return image2;
    }


    public static DocumentNumerised readCarteGrise(Mat imageAjust) throws Exception {
        ITesseract instance = new Tesseract();
        instance.setDatapath("/usr/share/tesseract-ocr/4.00/tessdata");

        try {
            String ocrResultRecto = instance.doOCR(matToBufferedImage(imageAjust));
            System.out.println("OCR Result:\n" + ocrResultRecto);
            // Traitement et affichage des données extraites
            return parseAndConvertInfoCarteGrise(ocrResultRecto);
        } catch (TesseractException e) {
            throw new Exception(e.getMessage());
        }
    }
    private static DocumentNumerised parseAndConvertInfoCarteGrise(String data){
        String nom = extractNom(data);
        String prenom = extractPrenom(data);
        String adresse = extractAdresse(data);
        String marqueTypeVehicule = extractMarqueModeleVehicule(data);
        String plaque = extractPlaque(data);
        String pays = extractPays(data);

        return new CarteGrise(nom,prenom,adresse,marqueTypeVehicule,plaque,pays);
    }


    private static String extractNom(String data) {
        String[] lines = data.split("\n");


        if (lines.length >= 3) {
            System.out.println("caca " + lines);
            String[] nameParts = lines[3].split("\\s+");
            if (nameParts.length > 1) {
                return nameParts[1]; // Prend le deuxième élément de la ligne
            }
        }
        return "Nom non trouvé";
    }

    private static String extractPrenom(String data) {
        String[] lines = data.split("\n");

        if (lines.length >= 4) {
            return lines[4].trim(); // Utilise toute la ligne du prénom
        }
        return "Prénom non trouvé";
    }

    public static String removeSpecificCharacters(String input, String charactersToRemove) {
        // Échappe les caractères spéciaux pour les utiliser dans une expression régulière
        String regexSafeCharacters = Pattern.quote(charactersToRemove);
        // Remplace les caractères spécifiques par une chaîne vide
        return input.replaceAll("[" + regexSafeCharacters + "]", "");
    }

    private static String extractAdresse(String data) {
        String adressePattern = "C\\.3\\s*(.+)\\s*(.+)";
        Pattern r = Pattern.compile(adressePattern);
        Matcher m = r.matcher(data);

        if (m.find()) {
            String temp = m.group(1).trim() + " " + m.group(2).trim();
            String charactersToRemove = "|,;:/-+()}={~@°!?azertyuiopmlkjhgfdsqwxcvbn";
            String resultString = removeSpecificCharacters(temp, charactersToRemove);
            return resultString;
        }
        return "Adresse non trouvée";
    }

    private static String extractMarqueModeleVehicule(String data) {
        String[] parts = data.split("CI<<");
        if (parts.length > 1) {
            String[] elements = parts[1].split("<+");
            List<String> filteredElements = Arrays.stream(elements)
                    .filter(element -> !element.trim().isEmpty())
                    .collect(Collectors.toList());

            if (filteredElements.size() >= 2) {
                // Les deux premiers éléments significatifs après "CI<<" devraient être la marque et le modèle
                return filteredElements.get(0).trim() + " " + filteredElements.get(1).trim();
            }
        }
        return "Marque/Modèle non trouvé";
    }

    private static String extractPlaque(String data) {
        String plaquePattern = "A\\.\\s*([A-Z0-9-]+)";
        Pattern r = Pattern.compile(plaquePattern);
        Matcher m = r.matcher(data);

        if (m.find()) {
            return m.group(1);
        }
        return "Plaque non trouvée";
    }

    private static String extractPays(String data) {
        String paysPattern = "FRA";
        if (data.contains(paysPattern)) {
            return "FRANCE";
        }
        return "Pays non trouvé";
    }

    private DocumentNumerised readCNIRecto(Mat imageAjust) throws Exception {

        ITesseract instance = new Tesseract();
        instance.setDatapath("/usr/share/tesseract-ocr/4.00/tessdata");

        try {
            String ocrResultRecto = instance.doOCR(matToBufferedImage(imageAjust));
            System.out.println("OCR Result:\n" + ocrResultRecto);
            // Traitement et affichage des données extraites
            return parseAndPrintDataCNIRecto(ocrResultRecto);
        } catch (TesseractException e) {
            throw new Exception(e.getMessage());
        }
    }

    private DocumentNumerised parseAndPrintDataCNIRecto(String ocrResultRecto) {
        // Diviser la chaîne de données en lignes
        String[] lines = ocrResultRecto.split("\n");

        // Vérifier si la quatrième ligne existe
        if (lines.length >= 20) {
            // Récupérer la quatrième ligne
            String nom = lines[4].trim();
            String prenom = lines[8].trim();
            String sexe = lines[11].trim();

            sexe = sexe.replace(" ", "");
            if (!sexe.isEmpty()) {
                sexe = sexe.substring(0, 1);
            }

            String nationalite = lines[11].trim();
            if (!nationalite.isEmpty()) {
                nationalite = nationalite.substring(2, 5);
            }

            String dateNaissance = lines[11].trim();
            dateNaissance = dateNaissance.replace(" ", "");
            if (!dateNaissance.isEmpty()) {
                dateNaissance = dateNaissance.substring(dateNaissance.length() - 8);
            }

            String lieu = lines[15].trim();

            String numDoc = lines[18].trim();
            if (!numDoc.isEmpty()) {
                numDoc = numDoc.substring(0,9);
            }

            String dateExpir = lines[18].trim();
            if (!dateExpir.isEmpty()) {
                dateExpir = dateExpir.substring(dateExpir.length() - 8);
            }


            String chiffreDuBas = lines[20].trim();
            if (!chiffreDuBas.isEmpty()) {
                chiffreDuBas = chiffreDuBas.substring(chiffreDuBas.length() - 6);
            }


            CarteIdDTO infoCarteId = new CarteIdDTO();
            infoCarteId.setNom(nom);
            infoCarteId.setPrenom(prenom);
            infoCarteId.setSexe(sexe);
            infoCarteId.setNationalite(nationalite);
            infoCarteId.setDateNaissance(dateNaissance);
            infoCarteId.setLieuNaissance(lieu);
            infoCarteId.setNumDoc(numDoc);
            infoCarteId.setDateExpiration(dateExpir);
            infoCarteId.setNumDuBas(chiffreDuBas);

            System.out.println("NOm :"+infoCarteId.getNom());
            System.out.println("prenom :"+infoCarteId.getPrenom());
            System.out.println("sexe :"+infoCarteId.getSexe());
            System.out.println("nationalité :"+infoCarteId.getNationalite());
            System.out.println("date naissance :"+infoCarteId.getDateNaissance());
            System.out.println("lieu naissance :"+infoCarteId.getLieuNaissance());
            System.out.println("num doc :"+infoCarteId.getNumDoc());
            System.out.println("date expi :"+infoCarteId.getDateExpiration());
            System.out.println("Num du bas :"+infoCarteId.getNumDuBas());



            // Supprimer tous les caractères "|" et les espaces
//            nom = nom.replace("|", "").replace(" ", "");

            return infoCarteId;
        }

        return null;
    }

    private Mat cropAndAdjustCNI(byte[] imageSrc,int thresh){
        // Charger la bibliothèque OpenCV
        OpenCV.loadLocally();

        // Charger l'image
        Mat image = Imgcodecs.imdecode(new MatOfByte(imageSrc),Imgcodecs.IMREAD_COLOR);

        //Recadrer l'image dynamiquement
        // Convertir en niveaux de gris et appliquer un seuillage
        Mat gray = new Mat();
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(gray, gray, thresh, 255, Imgproc.THRESH_BINARY_INV);

        // Détecter les contours
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(gray, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Filtrer les contours et trouver le plus grand
        double maxArea = 0;
        MatOfPoint maxContour = new MatOfPoint();
        for (MatOfPoint contour : contours) {
            double area = Imgproc.contourArea(contour);
            if (area > maxArea && area > 1000) { // Vérifie aussi que le contour est suffisamment grand
                maxArea = area;
                maxContour = contour;
            }
        }

        // Vérifier si un contour significatif a été trouvé
        Mat croppedImageDeb = new Mat();
        if (maxArea > 0) {
            // Recadrer l'image
            Rect rect = Imgproc.boundingRect(maxContour);
            croppedImageDeb = new Mat(image, rect);
        }

        // Récupérer les dimensions de l'image
        int width = croppedImageDeb.cols();
        int height = croppedImageDeb.rows();

        Rect cropRect = new Rect(1000, 0, width - 1100, height);

        // Recadrer l'image
        Mat croppedImage = new Mat(croppedImageDeb, cropRect);


        // Convertir l'image en noir et blanc
        Mat grayscaleImage = new Mat();
        Imgproc.cvtColor(croppedImage, grayscaleImage, Imgproc.COLOR_BGR2GRAY);

        // Passer l'exposition à 100%
        Mat adjustedImage = new Mat();
        grayscaleImage.convertTo(adjustedImage, -1, 1.5, 0); // L'exposition est multipliée par 2



        return adjustedImage;



    }


}
