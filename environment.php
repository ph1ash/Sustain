<!DOCTYPE HTML>
<!-- Previously right-sidebar.html-->
<html>
	<!--Head-->
	<?php include 'head.php'; ?>
	
	<body class="right-sidebar loading">
	
		<!-- Header -->
			<?php include 'header.php';?>
		
		<!-- Main -->
			<article id="main">	
				<!-- One -->
					<section class="wrapper style4 container">
					
						<div class="row oneandhalf">
							<div class="8u">
								
								<!-- Content -->
									<header>
										<h3>Environment Statistics</h3>
									</header>
									<div class="content">
										<p>Current Temperature</br>
                                                                                <?php include('./temperature.php'); echo $temperature,'&deg;F'?></br>
                                                                                Current Humidity</br>
                                                                                <?php include('./humidity.php'); echo $humidity,'%'?></br>
                                                                                Fan State</br>
                                                                                <?php include('./fanstate.php'); echo $fanstate ?></br>
										</p>
									</div>
	
							</div>
						</div>					
					</section>
			</article>

		<!-- Footer -->
			<?php include 'footer.php'; ?>

	</body>
</html>
