# BrickQuotes
BrickQuotes is a simple Linked Data API and allows to request quotations for Lego® bricks from the [bricklink](http://www.bricklink.com) online marketplace.
BrickQuotes is compliant with the [GoodRelations](http://www.heppnetz.de/projects/goodrelations/) Web vocabulary for e-commerce.

For the future, we plan to extend BrickQuotes to request quotations from more marketplaces.

## Installation & Running
Run BrickQuotes on your localhost by typing
```
mvn clean generate-resources license:add-third-party package tomcat7:run
```

You can modify BrickQuotes's listening port in the `pom.xml`
```
<build>
	...
	<plugins>
		...
		<plugin>
			<groupId>org.apache.tomcat.maven</groupId>
			<artifactId>tomcat7-maven-plugin</artifactId>
			<version>2.2</version>  	
            		<configuration>
				<server>BrickQuotes</server>
				<port>8080</port>
				<path>/</path>
			</configuration>
		</plugin>
		...
	</plugins>
</build>
```

## Usage
Get BrickQuotes up and running. HTTP GET requests can be made against BrcikQuotes's form-style GET API as follows
```
GET brick/{id} HTTP/1.1
Server: http:localhost:8080
Accept: text/turtle
```
Where `id` is your [Lego®](http://www.lego.com/) design identifier, [LDraw](http://www.ldraw.org/) identifier or [bricklink](http://bricklink.com/) identifier.

Need some examples? The following request 
```
GET brick/60475 HTTP/1.1
Server: http:localhost:8080
Accept: text/turtle
```
will give you a quotation for a [horizontal 1x1 brick with holder](https://sh-s7-live-s.legocdn.com/is/image/LEGOPCS/4533763_s1?$PABspin$)
```
@prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .
@prefix dcterms: <http://purl.org/dc/terms/> .
@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .
@prefix GR:    <http://purl.org/goodrelations/v1#> .

<http://localhost:8080/brick/60475>
        a                        GR:UnitPriceSpecification ;
        GR:hasCurrency           "EUR" ;
        GR:hasCurrencyValue      "0.0093"^^xsd:float ;
        GR:hasUnitOfMeasurement  "H87" ;
        GR:validFrom             "2016-10-11T10:41:26.937Z"^^xsd:dateTime .
```

Using
```
GET minifig/41063 HTTP/1.1
Server: http:localhost:8080
Accept: text/turtle
```
you can request a quotation for [Ariel Mermaid with flower in hair](http://img.bricklink.com/ItemImage/MN/0/dp014.png) ;-) 
 

## Contributing
Contributions are very welcome. Feel free to contact us.

## License
BrickQuotes is subject to the license terms in the LICENSE file found in the top-level directory of this distribution.
You may not use this file except in compliance with the License.

## Third-party Contents
This source distribution includes the third-party items with respective licenses as listed in the THIRD-PARTY file found in the top-level directory of this distribution.

## Acknowledgements
This work has been supported by the [German Ministry for Education and Research (BMBF)](http://www.bmbf.de/en/index.html) (FZK 01IMI3001 J) as part of the [ARVIDA](http://www.arvida.de/) project.