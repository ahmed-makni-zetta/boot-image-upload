package com.javainuse.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.javainuse.db.ImageRepository;
import com.javainuse.model.ImageModel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping(path = "image")
public class ImageUploadController {

	private static String UPLOADED_FOLDER = "E://";

	@Autowired
	ImageRepository imageRepository;


	@PostMapping("/upload")
	public ResponseEntity<List<String>> uplaodImage(@RequestParam("imageFile") MultipartFile file) throws IOException, ParserConfigurationException, SAXException {


		System.out.println("Original Image Byte Size - " + file.getBytes().length);
		byte[] bytes = file.getBytes();
		Path path = Paths.get(UPLOADED_FOLDER + file.getOriginalFilename());
		System.out.println("+++++++++  "+path);
		Files.write(path, bytes);


		ImageModel img = new ImageModel(file.getOriginalFilename(), file.getContentType(),
				compressZLib(file.getBytes()));
		imageRepository.save(img);


		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(path.toString());
		NodeList list = doc.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			Node n = list.item(i);
			Element e = (Element) n;

			List<String> cre = new ArrayList<>();
			//cre.add(e.getElementsByTagName("Name").item(0).getTextContent());
			//cre.add(e.getElementsByTagName("Position").item(0).getTextContent());
			//cre.add(e.getElementsByTagName("Business-field").item(0).getTextContent());
			//cre.add(e.getElementsByTagName("Class").item(0).getTextContent());
			//cre.add(e.getElementsByTagName("Data-type").item(0).getTextContent());
			//cre.add(e.getElementsByTagName("Definition").item(0).getTextContent());
			//cre.add(e.getElementsByTagName("Décimal-Définition").item(0).getTextContent());
			//cre.add(e.getElementsByTagName("Access-key-to").item(0).getTextContent());
			//cre.add(e.getElementsByTagName("Label").item(0).getTextContent());
			cre.add(n.getNodeName() + ":" + n.getTextContent().replaceAll("\n  "," "));
			Files.delete(path);
			return new ResponseEntity<List<String>>(cre, HttpStatus.OK);
			//return Response.status(Response.Status.OK).type(MediaType.APPLICATION_XML).entity(cre).build();
		}
		return  new ResponseEntity<List<String>>(HttpStatus.BAD_REQUEST);

	}

	@GetMapping(path = { "/get/{imageName}" })
	public ImageModel getImage(@PathVariable("imageName") String imageName) throws IOException {

		final Optional<ImageModel> retrievedImage = imageRepository.findByName(imageName);
		ImageModel img = new ImageModel(retrievedImage.get().getName(), retrievedImage.get().getType(),
				decompressZLib(retrievedImage.get().getPicByte()));


		return img;
	}

	// compress the image bytes before storing it in the database
	public static byte[] compressZLib(byte[] data) {
		Deflater deflater = new Deflater();
		deflater.setInput(data);
		deflater.finish();

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
		byte[] buffer = new byte[16777215];
		while (!deflater.finished()) {
			int count = deflater.deflate(buffer);
			outputStream.write(buffer, 0, count);
		}
		try {
			outputStream.close();
		} catch (IOException e) {
		}
		System.out.println("Compressed Image Byte Size - " + outputStream.toByteArray().length);

		return outputStream.toByteArray();
	}

	// uncompress the image bytes before returning it to the angular application
	public static byte[] decompressZLib(byte[] data) {
		Inflater inflater = new Inflater();
		inflater.setInput(data);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
		byte[] buffer = new byte[1024];
		try {
			while (!inflater.finished()) {
				int count = inflater.inflate(buffer);
				outputStream.write(buffer, 0, count);
			}
			outputStream.close();
		} catch (IOException ioe) {
		} catch (DataFormatException e) {
		}
		return outputStream.toByteArray();
	}
}