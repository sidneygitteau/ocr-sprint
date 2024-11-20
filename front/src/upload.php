<?php
$file_name = $_FILES["fileToUpload"]["name"];
$file_tmp = $_FILES["fileToUpload"]["tmp_name"];
$type_of_file = $_POST["typeOfFile"];

//URL de l'API
$url = "http://piece-reader:8081/api/piecereader/conversion";
//Passage du type en query param
$query = http_build_query(
    array(
        'typeDocument'=>$type_of_file
    )
);

//Récupération de l'image
$imageData = file_get_contents($file_tmp);

// Configuration de la requête
$options = array(
    'http' => array(
        'method'  => 'POST',
        'header'  => 'Content-Type: image/jpeg',
        'content' => $imageData
    )
);

// Création du contexte de la requête
$context  = stream_context_create($options);

// Exécution de la requête
$result = file_get_contents($url."?".$query, false, $context);

// Vérification du résultat
if ($result === false) {
    // Gestion de l'erreur
    echo "Erreur lors de la requête HTTP";
} else {
    // Format du json reçu
    $data = json_decode($result, true);

    $type_display = "";

    switch ($type_of_file) {
        case "CNI":
            $type_display = "Carte d'identité";
            break;
        case "PERMIS_RECTO":
            $type_display = "Permis de conduire recto";
            break;
        case "PERMIS_VERSO":
            $type_display = "Permis de conduire verso";
            break;
        case "CARTE_GRISE":
            $type_display = "Carte grise";
            break;
        default:
            $type_display = "Type de fichier non reconnu.";
    }

    //Affichage
    echo '<div class="json-container" style="display: flex;flex-direction: column;align-items: center;justify-content: center;">';
    echo '<h1>Type de document : ' . $type_display . '</h1>';
    $output = '';
    foreach ($data as $key => $value) {
        $output .= $key . ': ' . $value . '</br>';
    }
    $output = rtrim($output, ', ');
    echo '<pre>' . $output . '</pre>';
    echo '</div>';
}