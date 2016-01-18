<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>
<!DOCTYPE html>
<html>
<head>
	<title>Home</title>
	<script src="https://ajax.googleapis.com/ajax/libs/jquery/2.1.3/jquery.min.js"></script>
	<script type="text/javascript" src="js/search.js"></script>
	<link href="css/styles.css" rel="stylesheet">
	<link href="css/demo.css" rel="stylesheet">
	<link href="css/searchMeme.css" rel="stylesheet" type="text/css" />
    <script src="js/jquery.searchMeme.js" type="text/javascript"></script>
</head>
<body>
<!-- <form method="POST" id="myForm" name="myForm" action="home"> -->
	<!-- <input type="text" placeholder="Search..." required>
	<input type="button" value="Search"> -->
	<!-- <input type="submit" value="Upload" id="submitBtn"/> -->
	<div class="wrapper">
        <div class="demo">
            <input type="text" id="search-orange" />
            <input style="margin-left: 100px;margin-top: 10px;" type="checkbox" id="showAll"/><h3 style="display: inline;color: bisque;">Show all results</h3>
            <div style="display: flex;">
            <h3 style="color: bisque;">Scoring Type: </h3>
	            <input class="radio" type="radio" name="scoringType" value="tf" checked="true"><h4 style="display: inline;color: bisque;">tf.idf</h4>
	            <input class="radio" type="radio" name="scoringType" value="pagerank"><h4 style="display: inline;color: bisque;">PageRank</h4>
	            <input class="radio" type="radio" name="scoringType" value="tf_pagerank"><h4 style="display: inline;color: bisque;">tf.idf & PageRank</h4>
            </div>
            <div class="clear">
            </div>
        </div>
    </div>
    <div class="panel" id="search-results">
        Search results here...
    </div>
    <!-- <table id="table" style="display: none;"></table> -->
<!-- </form> -->
</body>
</html>
