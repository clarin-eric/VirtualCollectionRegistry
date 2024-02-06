<?php
include 'vendor/autoload.php';
$client = new GuzzleHttp\Client([
    'allow_redirects'=> true,
]);
$url = 'http://webapp:8080/submit/extensional';
$headers = [
    'Content-Type' => 'application/x-www-form-urlencoded',
//    'Referer' => 'https://arche.acdh.oeaw.ac.at',
];
$body = http_build_query([
    'name' => 'ArcheCollection',
    'resourceUri' => json_encode([
        "uri"         => "https://id.acdh.oeaw.ac.at/legalkraus",
	"label"       => "Legalkraus",
	"description" => "In the FWF-funded project “Intertextuality in the Legal Papers of Karl Kraus. A Scholarly Digital Edition” (FWF project no. P 31138-G30), the legal papers of the Austrian satirist Karl Kraus (1874‒1936) will be edited, provided digitally and contextualized with Kraus’ oeuvre as a whole. This project is carried out by the ACDH-CH in cooperation with the Vienna City Library and the Ludwig Boltzmann Institute for History and Society.Although Kraus’s attitude towards the courts in the Habsburg period had been predominantly critical and he frequently attacked reactionary judges and biased jurors, the constitutional reform of the Austrian Republic in 1919 and the abolition of death penalty marked a decisive break for Kraus. He especially welcomed the reform of the Press Law of 1922, which marked the beginning of a growing fondness for litigation. In the same year, Oskar Samek became his lawyer. In the course of the following 15 years, they were involved in over 200 court actions together.The material documenting these actions is held by the Vienna City Library and will be edited in the course of this project. The ACDH-CH will provide the technical framework, develop the data model for this digital edition, and support the project’s outreach activities."
    ]),
]);
$request = new GuzzleHttp\Psr7\Request('POST', $url, $headers, $body);
$response = $client->send($request);
http_response_code($response->getStatusCode());
foreach ($response->getHeaders() as $name => $values) {
    if (in_array(strtolower($name), ['transfer-encoding'])) {
        continue;
    }
    foreach ($values as $i) {
        header("$name: $i");
        echo "$name = $i";
    }
}
echo (string) $response->getBody();
