package com.nazdaq.ja.controller;

import java.security.Principal;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.mysql.jdbc.Field;
import com.nazdaq.ja.model.AcademicDoc;
import com.nazdaq.ja.model.AcademicHistory;
import com.nazdaq.ja.model.CandidateInfo;
import com.nazdaq.ja.model.Department;
import com.nazdaq.ja.model.District;
import com.nazdaq.ja.model.Thana;
import com.nazdaq.ja.service.CommonService;
import com.nazdaq.ja.service.UserService;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class ApplicationController {

	@Autowired
	private UserService userService;

	@Autowired
	private CommonService commonService;

	private static final Logger logger = LoggerFactory.getLogger(ApplicationController.class);

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/applicationForm", method = RequestMethod.GET)
	public ModelAndView applicationForm(Model theModel, Principal principal) {

		if (principal == null) {
			return new ModelAndView("redirect:/login");
		}

		List<Department> departmentList = (List<Department>) (Object) commonService.getAllObjectList("Department");

		List<District> districtList = (List<District>) (Object) commonService.getAllObjectList("District");
		List<Thana> thanaList = (List<Thana>) (Object) commonService.getAllObjectList("Thana");

		theModel.addAttribute("departmentList", departmentList);
		theModel.addAttribute("districtList", districtList);
		theModel.addAttribute("thanaList", thanaList);
		return new ModelAndView("applicationForm");
	}

	@RequestMapping(value = "/savePersonalInfo", method = RequestMethod.POST)
	public ModelAndView savePersonalInfo(Principal principal, HttpServletRequest request,
			@RequestParam("personImage") MultipartFile imagefile,
			@RequestParam("files_for_SSC") MultipartFile[] filesForSSC,
			@RequestParam("files_for_HSC") MultipartFile[] filesForHSC) {

		if (principal == null) {
			return new ModelAndView("redirect:/login");

		}
		// Saving General Info

		String filePath = null;
		CandidateInfo candidateInfo = new CandidateInfo();

		String personName = request.getParameter("personName");
		String personEmail = request.getParameter("personEmail");
		String personPhn = request.getParameter("personPhn");
		String personPresentAdd = request.getParameter("personPresentAdd");
		String personPermanentAdd = request.getParameter("personPermanentAdd");
		String personFatherName = request.getParameter("personFatherName");
		String personMotherName = request.getParameter("personMotherName");
		long personNidNo = Integer.parseInt(request.getParameter("personNidNo"));
		long personPassNo = Integer.parseInt(request.getParameter("personPassNo"));

		candidateInfo.setName(personName);
		candidateInfo.setEmail(personEmail);
		candidateInfo.setPhn(personPhn);
		candidateInfo.setPresentAdd(personPresentAdd);
		candidateInfo.setPermanentAdd(personPermanentAdd);
		candidateInfo.setFatherName(personFatherName);
		candidateInfo.setMotherName(personMotherName);
		candidateInfo.setNidNo(personNidNo);
		candidateInfo.setPassNo(personPassNo);

		if (!imagefile.isEmpty()) {
			try {
				byte[] bytes = imagefile.getBytes();

				// Creating the directory to store file
				String rootPath = System.getProperty("catalina.home");
				File dir = new File("/upload/ejms");
				if (!dir.exists())
					dir.mkdirs();

				String fileNameType = imagefile.getOriginalFilename();
				String[] parts = fileNameType.split("\\.");
				String fileName = parts[0];
				String fileType = parts[1];

				Random random = new Random(System.nanoTime() % 100000);

				int randomInt = random.nextInt(1000000000);

				String finalFileName = fileName + "-" + randomInt + "." + fileType;

				// Create the file on server
				File serverFile = new File(dir.getAbsolutePath() + File.separator + finalFileName);

				filePath = finalFileName;

				BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(serverFile));
				stream.write(bytes);
				stream.close();

				logger.info("Server File Location=" + serverFile.getAbsolutePath());
				System.out.println("You successfully uploaded file=");

			} catch (Exception e) {
				System.out.println("You failed to upload " + " => " + e.getMessage());

			}
		} else {
			System.out.println("You failed to upload " + " because the file was empty.");
		}

		candidateInfo.setPersonImagePath(filePath);

		Integer genaraleInfoId = commonService.saveWithReturnId(candidateInfo);
		candidateInfo.setId(genaraleInfoId);
		candidateInfo=null;
		candidateInfo=(CandidateInfo) commonService.getAnObjectByAnyUniqueColumn("CandidateInfo", "id", genaraleInfoId.toString());
		
		String msCheckboxValue = request.getParameter("check_box_ms");
		
		if (msCheckboxValue != null) {

			// Saving Academic Info SSC

			String filePathSSC = null;

			AcademicHistory academicHistorySSC = new AcademicHistory();

			String degree_name_ssc = request.getParameter("degree_name_ssc");
			String institute_name_ssc = request.getParameter("institute_name_ssc");
			String ssc_grade = request.getParameter("ssc_grade");
			int ssc_passing_year = Integer.parseInt(request.getParameter("ssc_passing_year"));

			int district_ssc = Integer.parseInt(request.getParameter("district_ssc"));
			int thana_ssc = Integer.parseInt(request.getParameter("thana_ssc"));

			District districtSSC = (District) commonService.getAnObjectByAnyUniqueColumn("District", "id",
					request.getParameter("district_ssc"));
			Thana thanaSSC = (Thana) commonService.getAnObjectByAnyUniqueColumn("Thana", "id",
					request.getParameter("thana_ssc"));

			academicHistorySSC.setDegreeName(degree_name_ssc);
			academicHistorySSC.setInstituteName(institute_name_ssc);
			academicHistorySSC.setGrade(ssc_grade);
			academicHistorySSC.setPassingYear(ssc_passing_year);
			academicHistorySSC.setDistrict(districtSSC);
			academicHistorySSC.setThana(thanaSSC);
			academicHistorySSC.setCandidateInfo(candidateInfo);

			Integer academicHistorySSCid = commonService.saveWithReturnId(academicHistorySSC);
			
			AcademicHistory academicHistorySSCDB=(AcademicHistory) commonService.getAnObjectByAnyUniqueColumn("AcademicHistory", "id", academicHistorySSCid.toString());

			// commonService.saveOrUpdateModelObjectToDB(academicHistorySSC);

			/*
			 * AcademicHistory academicHistoryObSSC = (AcademicHistory) commonService
			 * .getAnObjectByAnyUniqueColumn("AcademicHistory", "id",
			 * academicHistorySSC.getId().toString());
			 */

			for (int i = 0; i < filesForSSC.length; i++) {
				AcademicDoc academicDocSSC = new AcademicDoc();

				MultipartFile fileSSC = filesForSSC[i];

				try {
					byte[] bytes = fileSSC.getBytes();

					// Creating the directory to store file
					String rootPath = System.getProperty("catalina.home");
					File dir = new File("/upload/ejms");
					if (!dir.exists())
						dir.mkdirs();

					// Create the file on server

					String fileNameType = fileSSC.getOriginalFilename();
					String[] parts = fileNameType.split("\\.");
					String fileName = parts[0];
					String fileType = parts[1];

					String finalFileName = fileName + "-" + genaraleInfoId + "." + fileType;
					File serverFile = new File(dir.getAbsolutePath() + File.separator + finalFileName);
					filePathSSC = finalFileName;
					BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(serverFile));
					stream.write(bytes);
					stream.close();
					System.out.println("You successfully uploaded file=");

					// Save SSC File Path to Database

					academicDocSSC.setDocPath(filePathSSC);
					academicDocSSC.setAcademicHistory(academicHistorySSCDB);

					commonService.saveOrUpdateModelObjectToDB(academicDocSSC);

				} catch (Exception e) {
					System.out.println("You failed to upload " + " => " + e.getMessage());
				}
			}

			// Saving Academic Info HSC

			String filePathHSC = null;

			AcademicHistory academicHistoryHSC = new AcademicHistory();

			String degree_name_hsc = request.getParameter("degree_name_hsc");
			String institute_name_hsc = request.getParameter("institute_name_hsc");
			String hsc_grade = request.getParameter("hsc_grade");
			int hsc_passing_year = Integer.parseInt(request.getParameter("hsc_passing_year"));

			int district_hsc = Integer.parseInt(request.getParameter("district_hsc"));
			int thana_hsc = Integer.parseInt(request.getParameter("thana_hsc"));

			District districtHSC = (District) commonService.getAnObjectByAnyUniqueColumn("District", "id",
					request.getParameter("district_hsc"));
			Thana thanaHSC = (Thana) commonService.getAnObjectByAnyUniqueColumn("Thana", "id",
					request.getParameter("thana_hsc"));

			academicHistoryHSC.setDegreeName(degree_name_hsc);
			academicHistoryHSC.setInstituteName(institute_name_hsc);
			academicHistoryHSC.setGrade(hsc_grade);
			academicHistoryHSC.setPassingYear(hsc_passing_year);
			academicHistoryHSC.setDistrict(districtHSC);
			academicHistoryHSC.setThana(thanaHSC);			
			academicHistoryHSC.setCandidateInfo(candidateInfo);
			
			Integer academicHistoryHSCid = commonService.saveWithReturnId(academicHistoryHSC);
			
			
					
			//commonService.saveOrUpdateModelObjectToDB(academicHistoryHSC);
			AcademicHistory academicHistoryHscDb = (AcademicHistory) commonService
					.getAnObjectByAnyUniqueColumn("AcademicHistory", "id", academicHistoryHSCid.toString());

			for (int i = 0; i < filesForHSC.length; i++) {
				AcademicDoc academicDocHSC = new AcademicDoc();

				MultipartFile fileHSC = filesForHSC[i];

				try {
					byte[] bytes = fileHSC.getBytes();

					// Creating the directory to store file
					String rootPath = System.getProperty("catalina.home");
					File dir = new File("/upload/ejms");
					if (!dir.exists())
						dir.mkdirs();

					// Create the file on server

					String fileNameType = fileHSC.getOriginalFilename();
					String[] parts = fileNameType.split("\\.");
					String fileName = parts[0];
					String fileType = parts[1];

					String finalFileName = fileName + "-" + genaraleInfoId + "." + fileType;
					File serverFile = new File(dir.getAbsolutePath() + File.separator + finalFileName);
					filePathHSC = finalFileName;
					BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(serverFile));
					stream.write(bytes);
					stream.close();
					System.out.println("You successfully uploaded file=");

					// Save HSC File Path to Database

					academicDocHSC.setDocPath(filePathHSC);
					academicDocHSC.setAcademicHistory(academicHistoryHscDb);

					commonService.saveOrUpdateModelObjectToDB(academicDocHSC);

				} catch (Exception e) {
					System.out.println("You failed to upload " + " => " + e.getMessage());
				}
			}

		} else {

		}

		return new ModelAndView("redirect:/deparmentList");
	}

}



