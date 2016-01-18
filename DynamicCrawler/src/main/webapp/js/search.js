/**
 * 
 */
$(document).ready(function() {
   var searchOrange = $('#search-orange').searchMeme({ onSearch: function (searchText) {
	   $.get( "search?searchStr="+$("#search-orange").val()+"&showAll="+$('#showAll').is(':checked') + "&scoringType="+$('[type=radio]:checked').val(), function( data ) {
		   console.log(data);
		   
		   var rowHtml = "";
		   if((typeof data) != undefined && data.length > 0) {
			   rowHtml += "<table id='table'><thead><tr id='result-header'>Search Results for '" +searchText+ "' :</tr></thead><tbody>";
			   $.each(data, function(key, val) {
				   rowHtml += "<tr> <td> "+ (parseInt(key)+1) +"<a class='url' target = '_blank' href='"+ val.url +"'>" + val.title + " </a> ";
				   //add snippet
				   var desc = val.desc;
				   if(desc != undefined && desc.length > 0) {
					   desc = updateDesc(desc, searchText);
					   rowHtml += "<br>&nbsp&nbsp&nbsp;<span class='snippet'>"+ desc +"</span> ";
				   }
				   rowHtml += " </td> <td> "+ val.score +" </td> </tr>";
			   });
			   rowHtml += "</tbody></table>";
		   } else {
			   // show no results msg
			   rowHtml += "<h3>No Results Found... Please try again with a different query</h3>";
		   }
		   $('#search-results').html(rowHtml);
		   $('#search-results').hide().fadeIn('slow')
		   var tableHeight = $('#table').css('height');
		   $('.panel').height(tableHeight);
		   $('.panel').height("+=50");
		   
		   searchOrange.searchMeme({ searchComplete: true });
		 });
   }
   , buttonPlacement: 'left', button: 'orange'
   });
   
   function updateDesc(input, qTerm) {
	    return input.replace(new RegExp('(^|\\s)(' + qTerm + ')(\\s|$)','ig'), '$1<span class=\'desc\'>$2</span>$3');
	}
});