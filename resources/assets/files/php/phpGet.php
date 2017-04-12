<?php
echo "<html>";
echo "<body>";
echo "<hr><b>GET data:</b><br>";
var_dump($_GET);
echo "<hr><b>POST data:</b>";
var_dump($POST);
echo "<hr><b>POST data_raw:</b>";
var_dump($HTTP_RAW_POST_DATA);
echo "<form action=\"/phpGet.php\" method=\"POST\">";
echo "  <input name=\"fileupload\" value=\"fileupload\" id=\"fileupload\" type=\"file\">";
echo "  <label for=\"fileupload\"> Select a file to upload</label>";
echo "  <input value=\"submit\" type=\"submit\">";
echo "</form>";
echo "<hr><b>Special file data dump test:</b><br>";
$post = file_get_contents('php://input');
var_dump($post);
echo "<br></body>";
echo "</html>";
?>
