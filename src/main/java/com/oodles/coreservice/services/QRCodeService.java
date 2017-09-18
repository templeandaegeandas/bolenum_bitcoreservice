package com.oodles.coreservice.services;

/**
 * A service that has methods to generate QR code
 * @author Nimish Karan
 */
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.oodles.coreservice.conf.EnvConfiguration;
import com.oodles.coreservice.dao.AddressInfoDao;
import com.oodles.coreservice.domain.AddressInfo;

@Service
public class QRCodeService {
	@Autowired
	EnvConfiguration environment;
    @Autowired
    AddressInfoDao addressInfoDao;
	AtomicInteger windowSize = new AtomicInteger(3);
	@Value("${bitcoinCoreService.QRcode.location}")
	private String filePath;
	String sameFilename=null;
	/**
	 * Generate QR code for wallet address
	 * @param address
	 * @return
	 * @throws URISyntaxException
	 * @throws WriterException
	 * @throws IOException
	 */
	public Map<String, Object> qrCodeGeneration(String address)
			throws URISyntaxException, WriterException, IOException {


		UUID key = UUID.randomUUID();
		String uuid = key.toString();
		String file_path = filePath+"/"+uuid+".png";
		String file_name= uuid+".png";
		AddressInfo addressInfo=addressInfoDao.findByAddress(address);
		
		if(addressInfo==null){
		String charset = "UTF-8"; // or "ISO-8859-1"
		Map<EncodeHintType, ErrorCorrectionLevel> hintMap = new HashMap<EncodeHintType, ErrorCorrectionLevel>();
		hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
		Map<String, Object> map = new HashMap<String, Object>();
		
		createQRCode(address, file_path, charset, hintMap, 200, 200);
		//AddressInfo address_info=new AddressInfo();
		//address_info.setQrCodeFilename(file_name);
		//address_info.setAddress(address);
		//address_info.setLebel("Label this address");
		//address_info.setWalletUuid(walletUuid);
		//addressInfoDao.save(address_info);
		map.put("file_name", file_name);
		map.put("address", address);
		return map;
		}else
			 sameFilename=addressInfo.getQrCodeFilename();
		Map<String, Object> Samemap = new HashMap<String, Object>();
		Samemap.put("address", address);
		Samemap.put("file_name", sameFilename);
		return Samemap;
	}
	/**
	 * Core method to create a QR code
	 * @param qrCodeData
	 * @param filePath
	 * @param charset
	 * @param hintMap
	 * @param qrCodeheight
	 * @param qrCodewidth
	 * @throws WriterException
	 * @throws IOException
	 */
	public static void createQRCode(String qrCodeData, String filePath, String charset, Map<EncodeHintType,ErrorCorrectionLevel> hintMap, int qrCodeheight,
			int qrCodewidth) throws WriterException, IOException {
		BitMatrix matrix = new MultiFormatWriter().encode(new String(qrCodeData.getBytes(charset), charset),
				BarcodeFormat.QR_CODE, qrCodewidth, qrCodeheight, hintMap);
		MatrixToImageWriter.writeToFile(matrix, filePath.substring(filePath.lastIndexOf('.') + 1), new File(filePath));
	}
	public Map<String, Object> NewqrCodeGeneration(String address)
			throws URISyntaxException, WriterException, IOException {

		AddressInfo address_info=addressInfoDao.findByAddress(address);
		UUID key = UUID.randomUUID();
		String uuid = key.toString();
		String file_path = filePath + "/" + uuid + ".png";
		String file_name= uuid+".png";
		String charset = "UTF-8"; // or "ISO-8859-1"
		Map<EncodeHintType, ErrorCorrectionLevel> hintMap = new HashMap<EncodeHintType, ErrorCorrectionLevel>();
		hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
		Map<String, Object> map = new HashMap<String, Object>();
		createQRCode(address, file_path, charset, hintMap, 200, 200);
		address_info.setQrCodeFilename(file_name);
		address_info.setAddress(address);
		addressInfoDao.save(address_info);
		map.put("file_name", file_name);
		map.put("address", address);
		return map;
	}
}
