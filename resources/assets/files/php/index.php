<?php
//******************************* Lizenzbestimmungen *******************************//
//                                                                                  //
//  Der Quellcode von diesen Forum ist urheberrechtlich geschützt.                     //
//  Bitte beachten Sie die AGB auf www.frank-karau.de/agb.php                       //
//                                                                                  //
//  Dieser Lizenzhinweis darf nicht entfernt werden.                                //
//                                                                                  //
//  (C) phpFK - Forum ohne MySQL - www.frank-karau.de - support@frank-karau.de      //
//                                                                                  //
//**********************************************************************************//

$version = 'phpFK Lite-Version';
error_reporting(E_ERROR | E_WARNING | E_PARSE);
define('DIR', '../');
define('SICHERHEIT_FORUM', true);

$_TEXT = array();

$_TEXT['GO'] = 'Weiter &raquo;';
$_TEXT['STEP'] = 'Installation - Step';

$_TEXT['STEP2_HEAD'] = 'System-Analyse';
$_TEXT['STEP2_OPT1'] = 'PHP-Version';
$_TEXT['STEP2_OPT1_ERR'] = '<b>Fehler:</b> Sie ben&ouml;tigen mindestens Version 5.0, um das Forum betreiben zu k&ouml;nnen.';
$_TEXT['STEP2_OPT2'] = 'Zugriffsrechte';
$_TEXT['STEP2_OPT2_ERR'] = '<b>Fehler:</b> Bitte geben Sie das Verzeichnis <b>/data</b> und <b>/plugins</b> mit CHMOD 0777 frei. Klicken Sie danach auf <b>Aktualisieren</b>.';
$_TEXT['STEP2_OPT3'] = 'Safe-Mode';
$_TEXT['STEP2_OPT3_ERR'] = '<b>Hinweis:</b> Da Safe-Mode aktiviert ist, müssen Sie folgende Verzeichnisse mit Ihrem FTP-Client manuell erstellen und mit CHMOD 0777 freigeben: <b>data/user, data/upload, data/0, data/1, ..., data/9</b>. Klicken Sie danach auf "Weiter".';

$_TEXT['STEP3_HEAD1'] = 'Administrator';
$_TEXT['STEP3_OPT11'] = 'Benutzername';
$_TEXT['STEP3_OPT11_ERR'] = 'Der Benutzername ist ung&uuml;ltig oder schon registriert. Es sind nur Namen mit 3-20 Zeichen (a-Z 0-9 -_.,:) zulässig.</small>';
$_TEXT['STEP3_OPT12'] = 'Passwort';
$_TEXT['STEP3_OPT12_ERR'] = 'Das Passwort muss 5-10 Zeichen haben.';
$_TEXT['STEP3_OPT13'] = 'E-Mail-Adresse';
$_TEXT['STEP3_HEAD2'] = 'Konfiguration';
$_TEXT['STEP3_OPT21'] = 'Name des Forums';
$_TEXT['STEP3_OPT22'] = 'URL des Forums (ohne / am Ende)';

$_TEXT['STEP4_HEAD'] = 'Installation beendet';
$_TEXT['STEP4_TEXT'] = 'Bitte l&ouml;schen sie das Verzeichnis <b>install</b> und loggen sich danach als Administrator im <a href="../ap/">Administration-Panel</a> ein, um Ihr Forum zu konfigurieren.';

if ($_POST['lang'] == 'en') {
	$_TEXT['GO'] = 'Next &raquo;';
	$_TEXT['STEP'] = 'Installation - Step';

	$_TEXT['STEP2_HEAD'] = 'System analysis';
	$_TEXT['STEP2_OPT1'] = 'PHP version';
	$_TEXT['STEP2_OPT1_ERR'] = '<b>Error:</b> To run the forum you need at least PHP-Version 5.0.';
	$_TEXT['STEP2_OPT2'] = 'Read and write permission';
	$_TEXT['STEP2_OPT2_ERR'] = '<b>Error:</b> Please set the read and write permission of the folder <b>/data</b> and <b>/plugins</b> true (chmod 777) with your ftp-client and reload this page.';
	$_TEXT['STEP2_OPT3'] = 'Safe-Mode';
	$_TEXT['STEP2_OPT3_ERR'] = '<b>Notice:</b> Because Safe-mode is activated, you have to create the following folder manually and set the read an write permissions true (chmod 777): <b>data/user, data/upload, data/0, data/1, ..., data/9</b>. Then click "Next".';

	$_TEXT['STEP3_HEAD1'] = 'Administrator';
	$_TEXT['STEP3_OPT11'] = 'Username';
	$_TEXT['STEP3_OPT11_ERR'] = 'User name not valid or already in use. Names must contain 3-20 characters (a-Z 0-9 -_.,:).';
	$_TEXT['STEP3_OPT12'] = 'Password';
	$_TEXT['STEP3_OPT12_ERR'] = 'The password must contain 5-10 characters.';
	$_TEXT['STEP3_OPT13'] = 'E-Mail';
	$_TEXT['STEP3_HEAD2'] = 'Configuration';
	$_TEXT['STEP3_OPT21'] = 'Forum name';
	$_TEXT['STEP3_OPT22'] = 'URL of the forum (without a slash at the end)';

	$_TEXT['STEP4_HEAD'] = 'Installation completed';
	$_TEXT['STEP4_TEXT'] = 'Please delete the folder <b>install</b> for security reason and enter <a href="../ap/">Administration-Panel</a> to administrate your forum.';
}
if ($_POST['lang'] == 'es') {
	$_TEXT['GO'] = 'Después &raquo;';
	$_TEXT['STEP'] = 'Instalación - Paso';

	$_TEXT['STEP2_HEAD'] = 'Análisis del sistema';
	$_TEXT['STEP2_OPT1'] = 'Version-PHP';
	$_TEXT['STEP2_OPT1_ERR'] = '<b>Error:</b> Para correr el forum necesita al menos PHP-Version 5.0.';
	$_TEXT['STEP2_OPT2'] = 'Permisos de Lectura y Escritura';
	$_TEXT['STEP2_OPT2_ERR'] = '<b>Error:</b> Por Favor ponga los permisos de lectura y escritura de las carpetas <b>/data</b> y <b>/plugins</b> true (chmod 7777) con su ftp-cliente y recargue esta página.';
	$_TEXT['STEP2_OPT3'] = 'SAFE-mode';
	$_TEXT['STEP2_OPT3_ERR'] = '<b>Aviso:</b> Porque SAFE-mode esta activado, tiene que crear la siguiente carpeta manualmente y establecer los permisos de escritura y lectura en "true" (chmod 7777).<br>Carpetas: data/user, data/upload, data/0, data/1, data/2, data/3, data/4, data/5, data/6, data/7, data/8, data/9';

	$_TEXT['STEP3_HEAD1'] = 'Administrador';
	$_TEXT['STEP3_OPT11'] = 'Nombre de usuario';
	$_TEXT['STEP3_OPT11_ERR'] = 'El nombre de usuario no es válido ó ya esta registrado. Solo son válidos los nombres con 3-20 simbolos (a-Z 0-9 -_.,:).';
	$_TEXT['STEP3_OPT12'] = 'Contraseña';
	$_TEXT['STEP3_OPT12_ERR'] = 'Solo son válidos las contraseñas con 5-10 simbolos.';
	$_TEXT['STEP3_OPT13'] = 'Email';
	$_TEXT['STEP3_HEAD2'] = 'Parametros basicos';
	$_TEXT['STEP3_OPT21'] = 'Nombre del foro';
	$_TEXT['STEP3_OPT22'] = 'URL del foro (sin "/" al final)';

	$_TEXT['STEP4_HEAD'] = 'Instalación completa';
	$_TEXT['STEP4_TEXT'] = 'Suprimir por favor la carpeta <b>install</b> por razón de la seguridad y entrar en el <a href="../ap/">Administración-Panel</a> ein, para administrar el foro.';

}

require '../include/functions.php';

?><!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="content-type" content="text/html; charset=UTF-8">
<meta http-equiv="cache-control" content="no-cache">
<title>phpFK - PHP Forum ohne MySQL <?=$version?></title>
<link rel="stylesheet" type="text/css" href="../styles/default/text.css">
</head>
<body>
	<div id="forum_page">
	<table style="width:100%;">
	<tr>
		<td style="width:180px;"><img src="../styles/default/logo.png" alt="phpFK - Logo" align="left" /></td>
		<td style="width:auto;"><h1>phpFK - PHP-Forum ohne MySQL <?PHP echo $version; ?></h1></td>
	</tr></table>

	<div class="content">
		<form action="index.php" method="post">
		<input type="hidden" name="lang" value="<?PHP echo $_POST['lang']; ?>" />
		<table width="100%" class="main" cellspacing="0">
<?PHP
$phpver = phpversion();
@chmod("./../data", 0777);
@chmod("./../plugins", 0777);
$mode = ini_get("safe_mode");
if ($_POST['step'] == '3')
{
	if ($mode!="1")
	{
		if (!(is_dir("../data/user"))) @mkdir("../data/user", 0777);
		if (!(is_dir("../data/upload"))) @mkdir("../data/upload", 0777);
	}

	if (($mode=="1") && (!is_dir("../data/user")))
		$_POST['step'] = 2;
}
if ($_POST['step'] == '4')
{
	$allchars = "qwertzuiopasdfghjklyxcvbnmQWERTZUIOPASDFGHJKLYXCVBNM1234567890_-.,:";	
	for ($i = 0; $i < strlen($_POST['opt11']); $i++)
	{
		if (!is_numeric(strpos($allchars, substr($_POST['opt11'], $i, 1))))
		{
			$ERROR['opt11'] = true;
			$_POST['step'] = '3';
		}
	}	
	if ((strlen($_POST['opt11'])<3) OR (strlen($_POST['opt11'])>20))
	{
		$ERROR['opt11'] = true;
		$_POST['step'] = '3';
	}
	if (file_exists('../data/user/'.$_POST['opt11'].'.usr.ini'))
	{
		$ERROR['opt11'] = true;
		$_POST['step'] = '3';
	}
	if ((strlen($_POST['opt12'])<5) OR (strlen($_POST['opt12'])>10))
	{
		$ERROR['opt12'] = true;
		$_POST['step'] = '3';
	}
}

if ($_POST['step'] == '')
{
	echo '
		
		<tr><td class="oben">'.$_TEXT['STEP'].' 1</td></tr>
		<tr><td class="w">
			<input type="hidden" name="step" value="2" />
			<fieldset>
				<legend>Sprache | Language | Lengua&nbsp;</legend>
				<table><tr>
					<td style="width:50%; text-align:right;"><label for="lang">Bitte w&auml;hlen Sie Ihre Sprache aus: <br />Please select your language:<br />Seleccionar por favor su lengua:</label></td>
					<td style="width:50%;"><select name="lang" id="lang"><option value="de">Deutsch</option><option value="en">English</option><option value="es">Espanol</option></select></td>
				</tr></table>
			</fieldset>
		</td></tr>
		<tr><td class="g" style="text-align:center;"><input type="submit" name="submit" value="Weiter | Next | Después &raquo;" /></td></tr>
	';
}
if ($_POST['step'] == '2')
{
	$error = false;
	echo '
		<tr><td class="oben">'.$_TEXT['STEP'].' 2</td></tr>

		<tr><td class="w">
			<input type="hidden" name="step" value="3" />
			<fieldset>
				<legend>'.$_TEXT['STEP2_HEAD'].'&nbsp;</legend>
				<table>
				<tr>
					<td style="width:50%;">'.$_TEXT['STEP2_OPT1'].' <b>'.$phpver.'</b></td>
					<td style="width:50%;">
	';
		if (version_compare(phpversion(), "5.0") == -1)
		{
			echo '<img src="images/no.gif" /></td></tr><tr><td colspan="2"><div class="error">'.$_TEXT['STEP2_OPT1_ERR'].'</div>';
			$error = true;
		}
		else
		{
			echo '<img src="images/yes.gif" />';
		}
	echo '
					</td>
				</tr>
				<tr>
					<td style="width:50%;">'.$_TEXT['STEP2_OPT2'].'</td>
					<td style="width:50%;">
	';
		if (!is_writeable("../data") OR !is_writeable("../plugins"))
		{
			echo '<img src="images/no.gif" /></td></tr><tr><td colspan="2"><div class="error">'.$_TEXT['STEP2_OPT2_ERR'].'</div>';
			$error = true;
		}
		else
		{
			echo '<img src="images/yes.gif" />';
		}
	echo '
					</td>
				</tr>
				<tr>
					<td style="width:50%;">'.$_TEXT['STEP2_OPT3'].'</td>
					<td style="width:50%;">
	';
		if ($mode=="1")
		{
			echo '<img src="images/yes.gif" /></td></tr><tr><td colspan="2"><div class="notice">'.$_TEXT['STEP2_OPT3_ERR'].'</div>';
		}
		else
		{
			@mkdir('../data/user', 0777);
			@chmod('../data/user', 0777);
			@mkdir('../data/upload', 0777);
			@chmod('../data/upload', 0777);
			echo '<img src="images/yes.gif" />';
		}
	echo '
				</tr>
				</table>
			</fieldset>
		</td></tr>
		<tr><td class="g" style="text-align:center;"><input type="submit" name="submit" value="'.$_TEXT['GO'].'" '.($error?'disabled="disabled"':'').' /></td></tr>
	';
}
if ($_POST['step'] == '3')
{
	$_FORUM = IniLoad('../data/forum.ini');

	echo '
		<tr><td class="oben">'.$_TEXT['STEP'].' 3</td></tr>

		<tr><td class="w">
			<input type="hidden" name="step" value="4" />
			<fieldset>
				<legend>'.$_TEXT['STEP3_HEAD1'].'&nbsp;</legend>
				<table>
				<tr>
					<td style="width:50%; text-align:right;"><label for="opt11">'.$_TEXT['STEP3_OPT11'].':</label></td>
					<td style="width:50%;"><input type="text" id="opt11" name="opt11" value="'.$_POST['opt11'].'" />'.($ERROR['opt11']?'<br /><small><font color="red">'.$_TEXT['STEP3_OPT11_ERR'].'</font></small>':'').'</td>
				</tr>
				<tr>
					<td style="width:50%; text-align:right;"><label for="opt12">'.$_TEXT['STEP3_OPT12'].':</label></td>
					<td style="width:50%;"><input type="password" id="opt12" name="opt12" value="'.$_POST['opt12'].'" />'.($ERROR['opt12']?'<br /><small><font color="red">'.$_TEXT['STEP3_OPT12_ERR'].'</font></small>':'').'</td>
				</tr>
				<tr>
					<td style="width:50%; text-align:right;"><label for="opt13">'.$_TEXT['STEP3_OPT13'].':</label></td>
					<td style="width:50%;"><input type="text" id="opt13" name="opt13" value="'.$_POST['opt13'].'" /></td>
				</tr>
				</table>
			</fieldset>
			<fieldset>
				<legend>'.$_TEXT['STEP3_HEAD2'].'&nbsp;</legend>
				<table>
				<tr>
					<td style="width:50%; text-align:right;"><label for="opt21">'.$_TEXT['STEP3_OPT21'].':</label></td>
					<td style="width:50%;"><input type="text" size="30" id="opt21" name="opt21" value="'.($_POST['opt21']<>''?$_POST['opt21']:$_FORUM['settings_forum_name']).'" /></td>
				</tr>
				<tr>
					<td style="width:50%; text-align:right;"><label for="opt22">'.$_TEXT['STEP3_OPT22'].':</label></td>
					<td style="width:50%;"><input type="text" size="30" id="opt22" name="opt22" value="'.($_FORUM['settings_forum_url']<>''?$_FORUM['settings_forum_url']:str_replace('/install/index.php', '', 'http://'.$_SERVER['SERVER_NAME'].$_SERVER['SCRIPT_NAME'])).'" /></td>
				</tr>
				</table>
		</td></tr>	
		<tr><td class="g" style="text-align:center;"><input type="submit" name="submit" value="'.$_TEXT['GO'].'" /></td></tr>
	';

}
if ($_POST['step'] == '4')
{
	$data = IniLoad('../data/user/'.$_POST['opt11'].'.usr.ini');
	$data['name'] = $_POST['opt11'];
	$data['password'] = md5($_POST['opt12']);
	$data['email'] = $_POST['opt13'];
	$data['register_date'] = time();
	$data['lastonline_date'] = time();
	$data['newsletter'] = true;
	$data['count_topics'] = 0;
	$data['count_answeres'] = 0;
	$data['count_answeres2'] = 0;
	$data['count_locked'] = 0;

	IniSave('../data/user/'.$_POST['opt11'].'.usr.ini', $data);

	$data = IniLoad('../data/user/Admins.grp.ini');
	AddToGroup($data['members'], $_POST['opt11']);
	IniSave('../data/user/Admins.grp.ini', $data);

	if (file_exists('../data/forum.ini'))
	{
		$_FORUM = IniLoad('../data/forum.ini');
		$_FORUM['settings_forum_name'] = stripslashes($_POST['opt21']);
		$_FORUM['settings_forum_url'] = $_POST['opt22'];
	}
	else
	{
		$_FORUM['settings_forum_name'] = stripslashes($_POST['opt21']);
		$_FORUM['settings_forum_header'] = stripslashes($_POST['opt21']);
		$_FORUM['settings_forum_url'] = $_POST['opt22'];
		if ($_POST['lang'] == 'de')
		{
			$_FORUM['settings_forum_language'] = 'de.php';
		}
		else if ($_POST['lang'] == 'en')
		{
			$_FORUM['settings_forum_language'] = 'en.php';
		}
		else if ($_POST['lang'] == 'es')
		{
			$_FORUM['settings_forum_language'] = 'es.php';
		}

		$_FORUM['settings_design_style'] = 'default';
		$_FORUM['settings_design_showstat'] = true;
		$_FORUM['settings_design_showlast_guest'] = false;
		$_FORUM['settings_design_showlast_user'] = true;
		$_FORUM['settings_design_rss_count'] = 10;
		$_FORUM['settings_design_javascript'] = true;
		$_FORUM['settings_design_javascript_count'] = '10';
		$_FORUM['settings_design_lastposts_count'] = '10';
		$_FORUM['settings_design_rss'] = true;
		$_FORUM['settings_design_rss_count'] = '10';
		$_FORUM['settings_design_ranking_guest'] = true;
		$_FORUM['settings_design_ranking_user'] = true;

		$_FORUM['settings_navigation'] = '*0¿*1¿*2¿*3¿*4¿*5¿*6';

		$_FORUM['settings_system_upload_file'] = true;
		$_FORUM['settings_system_upload_file_formats'] = Array2Group(Array('jpg', 'gif', 'png', 'bmp', 'doc', 'xls', 'pdf', 'ppt'));
		$_FORUM['settings_system_upload_file_size'] = 1000;
		$_FORUM['settings_system_upload_avatar'] = true;
		$_FORUM['settings_system_upload_avatar_formats'] = Array2Group(Array('jpg', 'gif', 'png', 'bmp'));
		$_FORUM['settings_system_upload_avatar_size'] = 500;
		$_FORUM['settings_system_upload_avatar_pixel'] = 100;

		$_FORUM['settings_admin_edit'] = true;
		$_FORUM['settings_admin_notification'] = true;

		$_FORUM['settings_user_ranking2'] = '50';
		$_FORUM['settings_user_ranking3'] = '100';
		$_FORUM['settings_user_ranking4'] = '250';
		$_FORUM['settings_user_ranking5'] = '500';
		$_FORUM['settings_timeformat'] = 'd.m.y H:i';
		$_FORUM['settings_directanswer'] = true;
		$_FORUM['settings_loading'] = 'premium';

	}
	$_FORUM['status'] = true;
	$_FORUM['version'] = $version;
	$_FORUM['update_date'] = date("d.m.y H:i");
	IniSave('../data/forum.ini', $_FORUM);

	if (!file_exists('../data/navigation.ini'))
	{
		$nav_ini = array();
		$nav_ini['order'] = '*0¿*1¿*2¿*3¿*4¿*5¿*6';
		IniSave('../data/navigation.ini', $nav_ini);
	}

	@mail("support@frank-karau.de", "Installation des Forums", "Server: ".getenv("SERVER_NAME")."\nVerzeichnis: ".getcwd()." \nDatum: ".date("d.m.Y H:i")."\nPHP-Version: $phpver \nSAFE-Mode: $mode\nForum-Version: $version\n---------------------------\n", "");

	echo '
		<tr><td class="oben">'.$_TEXT['STEP'].' 4</td></tr>

		<tr><td class="w">
			<fieldset>
				<legend>'.$_TEXT['STEP4_HEAD'].'&nbsp;</legend>
				<div id="confirm" style="margin:10px;">'.$_TEXT['STEP4_TEXT'].'</div>
			</fieldset>
		</td></tr>
	';
}
?>
		</table>
		</form>
	</div>
	<div id="copy">
		Powered by: <a href="http://www.frank-karau.de" target="_blank"> phpFK - PHP-Forum ohne MySQL - <?PHP echo $version; ?></a> | <a href="http://www.frank-karau.de" target="_blank">Frank Karau</a> | <a href="http://www.frank-karau.de/shop/" target="_blank">phpFK Shop & Vollversion kaufen</a>

	</div>
</div></center>
</body></html>
