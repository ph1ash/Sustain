<?php
	$humidity = $_GET["humid"];
	$h_var_str = var_export($humidity, true);
	$h_var = "<?php\n\n\$humidity = $h_var_str;\n\n?>";
	file_put_contents('humidity.php',$h_var);
	$temperature = $_GET["temp"];
	$t_var_str = var_export($temperature,true);
	$t_var = "<?php\n\n\$temperature = $t_var_str;\n\n?>";
	file_put_contents('temperature.php',$t_var);
	$fanstate = $_GET["fan"];
	if ($fanstate == '1')
	{
		$fanstate = "Fan Running";
	}
	else
	{
		$fanstate = "Fan Off";
	}
	$f_var_str = var_export($fanstate, true);
	$f_var = "<?php\n\n\$fanstate = $f_var_str;\n\n?>";
	file_put_contents('fanstate.php',$f_var);
	echo $humidity, ' ', $temperature, ' ', $fanstate;
?>
