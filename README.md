# My-Search-Engine
A web crawler for .edu sites and a search engine with PageRank algorithm

To run the crawler, please place the war file in tomcat/webapps folder and run startup.sh to start tomcat.
The path to the seed URLS, indexing path and output path has to be set in the file 'config.properties' under src/main/resources folder of the project.
To not crawl every time please comment out the property "seedPath" only.

After placing the war file under tomcat/webapps folder and changing the config.properties, please start the server and enter the following url in a browser.
http://localhost:8090/DynamicCrawler/home
