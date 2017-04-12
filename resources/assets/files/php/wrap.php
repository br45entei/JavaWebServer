<?php
/** Wrapper.php
 **
 ** Description: Adds the $_GET hash to a shell-ran PHP script
 **
 ** Usage:       $ php Wrapper.php <yourscript.php> arg1=val1 arg2=val2 ...
**/

//Grab the filenames from the argument list
$scriptWrapperFilename = array_shift($argv); // argv[0]
$scriptToRunFilename = array_shift($argv); // argv[1]

// Set some restrictions
if (php_sapi_name() !== "cli")
    die(" * This should only be ran from the shell prompt!\n");

// define the $_GET hash in global scope
$_GET;

// walk the rest and pack the $_GET hash
foreach ($argv as $arg) {
    // drop the argument if it's not a key/val pair
    if(strpos($arg, "=") === false)
        continue;

    list($key, $value) = split("=", $arg);

    // pack the $_GET variable
    $_GET[$key] = $arg;
}

// get and require the PHP file we're trying to run
if (is_file($scriptToRunFilename))
    require_once $scriptToRunFilename;
else
    die(" * Could not open `$scriptToRunFilename' for inclusion.\n");

?>