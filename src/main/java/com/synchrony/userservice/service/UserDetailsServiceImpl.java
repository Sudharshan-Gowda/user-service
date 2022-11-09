package com.synchrony.userservice.service;

import static com.synchrony.userservice.common.UserDetailsConstant.BASE64_CONVERTION_FAILED;
import static com.synchrony.userservice.common.UserDetailsConstant.DEBUG;
import static com.synchrony.userservice.common.UserDetailsConstant.EMAIL_ID_EXIST;
import static com.synchrony.userservice.common.UserDetailsConstant.ERROR;
import static com.synchrony.userservice.common.UserDetailsConstant.IMAGE_UPLOAD_FAILED;
import static com.synchrony.userservice.common.UserDetailsConstant.INVALID_CREDENTIAL;
import static com.synchrony.userservice.common.UserDetailsConstant.INVALID_USER_ID;
import static com.synchrony.userservice.common.UserDetailsConstant.INVALID_USER_NAME;
import static com.synchrony.userservice.common.UserDetailsConstant.LOGIN_SUCCESS;
import static com.synchrony.userservice.common.UserDetailsConstant.MOBILE_NUMBER_EXIST;
import static com.synchrony.userservice.common.UserDetailsConstant.SOMETHING_WENT_WRONG;
import static com.synchrony.userservice.common.UserDetailsConstant.UPLOAD_IMAGE_FAILED;
import static com.synchrony.userservice.common.UserDetailsConstant.USER_NOT_FOUND;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.DatatypeConverter;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synchrony.userservice.dto.ImageResponse;
import com.synchrony.userservice.dto.UserDetailsDto;
import com.synchrony.userservice.dto.UserLoginRequestDto;
import com.synchrony.userservice.entity.UserDetails;
import com.synchrony.userservice.entity.UserImage;
import com.synchrony.userservice.entity.UserLogin;
import com.synchrony.userservice.exception.InvalidCredentialException;
import com.synchrony.userservice.exception.UnauthorizedException;
import com.synchrony.userservice.exception.UserDetailsException;
import com.synchrony.userservice.exception.UserNameExistException;
import com.synchrony.userservice.exception.WebException;
import com.synchrony.userservice.repository.UserDetailsRepository;
import com.synchrony.userservice.repository.UserLoginRepository;
import com.synchrony.userservice.response.AuthenticationResponse;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserDetailsServiceImpl implements UserDetailService {

	@Autowired
	private UserDetailsRepository userDetailsRepository;

	@Autowired
	private UserLoginRepository userLoginRepository;

	@Autowired
	private PasswordEncoder bcryptEncoder;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Value("${jwt.secret}")
	private String secret;

	private static final String CLIENT_ID = "ad7bdc6354bf3dc";

	public static final String UPLOAD_API_URL = "https://api.imgur.com/3/image";

	@Override
	public UserDetails registerUser(UserDetailsDto userDetailsDto) {
		log.debug(DEBUG, userDetailsDto);
		try {
			UserLogin userLogin = findByEmail(userDetailsDto.getEmailId());

			if (Objects.isNull(userLogin)) {
				validateMobileNumber(userDetailsDto.getMobileNumber());
				UserDetails userDetails = new UserDetails();
				BeanUtils.copyProperties(userDetailsDto, userDetails);
				UserLogin login = new UserLogin();
				login.setEmailId(userDetailsDto.getEmailId());
				login.setPassword(bcryptEncoder.encode(userDetailsDto.getPassword()));
				login.setUserRole("USER");
				userLoginRepository.save(login);
				return userDetailsRepository.save(userDetails);
			} else {
				throw new UserNameExistException(EMAIL_ID_EXIST);
			}
		} catch (UserNameExistException e) {
			log.error(ERROR, e);
			throw e;
		} catch (Exception e) {
			log.error(ERROR, e);
			throw new UserDetailsException(SOMETHING_WENT_WRONG);
		}
	}

	private UserLogin findByEmail(String emailId) {
		log.debug(DEBUG, emailId);
		return userLoginRepository.findByEmailId(emailId);
	}

	private void validateMobileNumber(String mobileNumber) {
		log.debug(DEBUG, mobileNumber);
		UserDetails user = userDetailsRepository.findByMobileNumber(mobileNumber);
		if (!Objects.isNull(user)) {
			throw new UserNameExistException(MOBILE_NUMBER_EXIST);
		}
	}

	@Override
	public AuthenticationResponse createAuthenticationToken(UserLoginRequestDto userLoginRequestPojo) {
		log.debug(DEBUG);
		String token = null;
		try {
			if (userLoginRequestPojo.getUserName().contains("@")) {
				UserDetails userDetails = userDetailsRepository.findByEmailId(userLoginRequestPojo.getUserName());
				if (!Objects.isNull(userDetails)) {
					UserLogin userLogin = findByEmail(userDetails.getEmailId());
					authenticate(userLoginRequestPojo.getUserName(), userLoginRequestPojo.getPassword());
					token = getToken(userLogin, userDetails.getUserId());
				} else {
					throw new UserNameExistException(INVALID_USER_NAME);
				}
			} else {
				UserDetails userDetails = userDetailsRepository.findByMobileNumber(userLoginRequestPojo.getUserName());
				if (!Objects.isNull(userDetails)) {
					UserLogin userLogin = findByEmail(userDetails.getEmailId());
					authenticate(userDetails.getEmailId(), userLoginRequestPojo.getPassword());
					token = getToken(userLogin, userDetails.getUserId());
				} else {
					throw new UserNameExistException(INVALID_USER_NAME);
				}
			}
			return new AuthenticationResponse(false, LOGIN_SUCCESS, token, null);
		} catch (DisabledException | BadCredentialsException | UnauthorizedException | InvalidCredentialException e) {
			log.error(ERROR, e);
			throw e;
		} catch (Exception e) {
			log.error(ERROR, e);
			throw new UserDetailsException(SOMETHING_WENT_WRONG);

		}
	}

	private void authenticate(String userName, String password)
			throws DisabledException, BadCredentialsException, UnauthorizedException, InvalidCredentialException {
		try {
			log.debug(DEBUG, userName);
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userName, password));
		} catch (DisabledException e) {
			log.error(ERROR, e);
			throw new UnauthorizedException(USER_NOT_FOUND);
		} catch (BadCredentialsException e) {
			log.error(ERROR, e);
			throw new InvalidCredentialException(INVALID_CREDENTIAL);
		}
	}

	private String getToken(UserLogin userLogin, long userId) {
		log.debug(DEBUG, userId);
		Algorithm algorithm = Algorithm.HMAC256(secret.getBytes());
		return JWT.create().withSubject(userLogin.getEmailId())
				.withExpiresAt(new Date(System.currentTimeMillis() + 90 * 60 * 1000))
				.withClaim("roles", userLogin.getUserRole()).sign(algorithm);
	}

	private String upload(File file) {
		log.debug(DEBUG);
		HttpURLConnection conn = getHttpConnection(UPLOAD_API_URL);
		writeToConnection(conn, "image=" + toBase64(file));
		return getResponse(conn);
	}

	private static HttpURLConnection getHttpConnection(String url) {
		log.debug(DEBUG);
		HttpURLConnection conn;
		try {
			conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Authorization", "Client-ID " + CLIENT_ID);
			conn.setReadTimeout(100000);
			conn.connect();
			return conn;
		} catch (UnknownHostException e) {
			log.error(ERROR, e);
			throw new WebException(UPLOAD_IMAGE_FAILED);
		} catch (IOException e) {
			log.error(ERROR, e);
			throw new WebException(IMAGE_UPLOAD_FAILED);
		}
	}

	private static String getResponse(HttpURLConnection conn) {
		log.debug(DEBUG);
		StringBuilder str = new StringBuilder();
		BufferedReader reader;
		try {
			if (conn.getResponseCode() != 200) {
				throw new WebException(IMAGE_UPLOAD_FAILED);
			}
			reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				str.append(line);
			}
			reader.close();
		} catch (IOException e) {
			log.error(ERROR, e);
			throw new WebException(IMAGE_UPLOAD_FAILED);
		}
		if (str.toString().equals("")) {
			throw new WebException(IMAGE_UPLOAD_FAILED);
		}
		return str.toString();
	}

	private static String toBase64(File file) {
		log.debug(DEBUG);
		try {
			byte[] b = new byte[(int) file.length()];
			FileInputStream fs = new FileInputStream(file);
			fs.read(b);
			fs.close();
			return URLEncoder.encode(DatatypeConverter.printBase64Binary(b), "UTF-8");
		} catch (IOException e) {
			throw new WebException(BASE64_CONVERTION_FAILED);
		}
	}

	private static void writeToConnection(HttpURLConnection conn, String message) {
		log.debug(DEBUG);
		OutputStreamWriter writer;
		try {
			writer = new OutputStreamWriter(conn.getOutputStream());
			writer.write(message);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			log.error(ERROR, e);
			throw new WebException(UPLOAD_IMAGE_FAILED);
		}
	}

	@Override
	public UserDetails uploadImages(long userId, List<MultipartFile> file) throws IOException {
		log.debug(DEBUG, userId);
		List<UserImage> arrayList = new ArrayList<>();
		file.forEach(e -> {
			try {
				File convFile = new File(e.getOriginalFilename());
				FileOutputStream fos = new FileOutputStream(convFile);
				fos.write(e.getBytes());
				fos.close();
				String upload = upload(convFile);
				UserImage userImage = new UserImage();
				ObjectMapper mapper = new ObjectMapper();
				mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
				ImageResponse readValue = mapper.readValue(upload, ImageResponse.class);
				userImage.setImageUrl(readValue.getData().getLink());
				userImage.setImgurId(readValue.getData().getId());
				arrayList.add(userImage);
			} catch (Exception e1) {
				log.error(ERROR, e1);
				throw new UserDetailsException(SOMETHING_WENT_WRONG);
			}
		});
		Optional<UserDetails> findById = userDetailsRepository.findById(userId);
		if (findById.isPresent()) {
			findById.get().getUserImages().addAll(arrayList);
			return userDetailsRepository.save(findById.get());
		} else {
			throw new UserNameExistException(INVALID_USER_ID);
		}
	}

	@Override
	public UserDetails getUserInfo(long userId) {
		log.debug(DEBUG, userId);
		Optional<UserDetails> findById = userDetailsRepository.findById(userId);
		if (findById.isPresent()) {
			return findById.get();
		} else {
			throw new UserNameExistException(INVALID_USER_ID);
		}
	}

	@Override
	public Object getImage(String id) throws IOException, InterruptedException {
		HttpClient client = HttpClient.newBuilder().build();
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://api.imgur.com/3/image/" + id))
				.header("Authorization", "Client-ID ad7bdc6354bf3dc").header("User-Agent", "Epicture").build();
		HttpResponse<?> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		return response.body();
	}

}
