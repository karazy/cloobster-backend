SET PDFJET_PATH=C:\Dev\karazy\PDFjet-Open-Source
cd %pdfjet_path%
build-java
mvn install:install-file -DgroupId=com.innovatics -DartifactId=pdfjet -Dversion=3.11 -Dpackaging=jar -Dfile=PDFjet.jar