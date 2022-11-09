package com.synchrony.userservice.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Objects;

import javax.xml.bind.DatatypeConverter;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.synchrony.userservice.dto.UserDetailsDto;
import com.synchrony.userservice.dto.UserLoginRequestDto;
import com.synchrony.userservice.entity.UserDetails;
import com.synchrony.userservice.entity.UserLogin;
import com.synchrony.userservice.exception.InvalidCredentialException;
import com.synchrony.userservice.exception.UnauthorizedException;
import com.synchrony.userservice.exception.UserDetailsException;
import com.synchrony.userservice.exception.UserNameExistException;
import com.synchrony.userservice.exception.WebException;
import com.synchrony.userservice.repository.UserDetailsRepository;
import com.synchrony.userservice.repository.UserLoginRepository;
import com.synchrony.userservice.response.AuthenticationResponse;

@Service
public class UserDetailsServiceImpl implements UserDetailService {

	@Autowired
	private UserDetailsRepository userDetailsRepository;

	@Autowired
	private UserLoginRepository userLoginRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Value("${jwt.secret}")
	private String secret;

	private static final String CLIENT_ID = "ad7bdc6354bf3dc";

	public static final String UPLOAD_API_URL = "https://api.imgur.com/3/image";

	@Override
	public UserDetails registerUser(UserDetailsDto userDetailsDto) {
		try {
			UserLogin userLogin = findByEmail(userDetailsDto.getEmailId());

			if (Objects.isNull(userLogin)) {
				validateMobileNumber(userDetailsDto.getMobileNumber());
				UserDetails userDetails = new UserDetails();
				BeanUtils.copyProperties(userDetailsDto, userDetails);
				UserLogin login = new UserLogin();
				login.setEmailId(userDetailsDto.getEmailId());
				login.setPassword(passwordEncoder.encode(userDetailsDto.getPassword()));
				login.setUserRole("USER");
				userLoginRepository.save(login);
				return userDetailsRepository.save(userDetails);
			} else {
				throw new UserNameExistException("Email Id Already Registered");
			}
		} catch (UserNameExistException e) {
			throw e;
		} catch (Exception e) {
			throw new UserDetailsException("Something Went Wrong...!");
		}
	}

	private UserLogin findByEmail(String emailId) {
		return userLoginRepository.findByEmailId(emailId);
	}

	private void validateMobileNumber(String mobileNumber) {
		UserDetails user = userDetailsRepository.findByMobileNumber(mobileNumber);
		if (!Objects.isNull(user)) {
			throw new UserNameExistException("Mobile Number Already Registered");
		}
	}

	@Override
	public AuthenticationResponse createAuthenticationToken(UserLoginRequestDto userLoginRequestPojo) {
		String token = null;
		try {
			if (userLoginRequestPojo.getUserName().contains("@")) {
				UserDetails userDetails = userDetailsRepository.findByEmailId(userLoginRequestPojo.getUserName());
				if (!Objects.isNull(userDetails)) {
					UserLogin userLogin = findByEmail(userDetails.getEmailId());
					authenticate(userLoginRequestPojo.getUserName(), userLoginRequestPojo.getPassword());
					token = getToken(userLogin, userDetails.getUserId());
				} else {
					throw new UserNameExistException("User Name Not Found");
				}
			} else {
				UserDetails userDetails = userDetailsRepository.findByMobileNumber(userLoginRequestPojo.getUserName());
				UserLogin userLogin = findByEmail(userDetails.getEmailId());
				if (!Objects.isNull(userDetails)) {
					authenticate(userDetails.getEmailId(), userLoginRequestPojo.getPassword());
					token = getToken(userLogin, userDetails.getUserId());
				} else {
					throw new UserNameExistException("User Name Not Found");
				}
			}
			return new AuthenticationResponse(false, "Login Successfull..!", token, null);
		} catch (DisabledException | BadCredentialsException | UnauthorizedException | InvalidCredentialException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new UserDetailsException("Something Went Wrong...!");

		}
	}

	private void authenticate(String userName, String password)
			throws DisabledException, BadCredentialsException, UnauthorizedException, InvalidCredentialException {
		try {
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userName, password));
		} catch (DisabledException e) {
			throw new UnauthorizedException("User Not Found");
		} catch (BadCredentialsException e) {
			throw new InvalidCredentialException("Invalid Credentials");
		}
	}

	private String getToken(UserLogin userLogin, long userId) {
		Algorithm algorithm = Algorithm.HMAC256(secret.getBytes());
		return JWT.create().withSubject(userId + "," + userLogin.getEmailId())
				.withExpiresAt(new Date(System.currentTimeMillis() + 90 * 60 * 1000))
				.withClaim("roles", userLogin.getUserRole()).sign(algorithm);
	}

	private String upload(File file) {
		HttpURLConnection conn = getHttpConnection(UPLOAD_API_URL);
		writeToConnection(conn, "image=" + toBase64(file));
		return getResponse(conn);
	}

	private static HttpURLConnection getHttpConnection(String url) {
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
			throw new WebException("");
		} catch (IOException e) {
			throw new WebException("");
		}
	}

	private static String getResponse(HttpURLConnection conn) {
		StringBuilder str = new StringBuilder();
		BufferedReader reader;
		try {
			if (conn.getResponseCode() != 200) {
				throw new WebException("");
			}
			reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				str.append(line);
			}
			reader.close();
		} catch (IOException e) {
			throw new WebException("");
		}
		if (str.toString().equals("")) {
			throw new WebException("");
		}
		return str.toString();
	}

	private static String toBase64(File file) {
		try {
			byte[] b = new byte[(int) file.length()];
			FileInputStream fs = new FileInputStream(file);
			fs.read(b);
			fs.close();
			return URLEncoder.encode(DatatypeConverter.printBase64Binary(b), "UTF-8");
		} catch (IOException e) {
			throw new WebException("");
		}
	}

	private static void writeToConnection(HttpURLConnection conn, String message) {
		OutputStreamWriter writer;
		try {
			writer = new OutputStreamWriter(conn.getOutputStream());
			writer.write(message);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			throw new WebException("");
		}
	}

}
