package com.mvc.servlet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.mvc.bean.Event;
import com.mvc.connection.DatabaseConnection;
import com.mvc.dao.EventDao;
import com.mvc.dao.UserDao;


@WebServlet("/EventSetupServlet")
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2, //2MB
maxFileSize = 1024 * 1024 * 10, //10MB
maxRequestSize = 1024 * 1024 * 50)

public class EventSetupServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String title=request.getParameter("title");
		String description=request.getParameter("description");
		String startdate=request.getParameter("startdate");
		String enddate=request.getParameter("enddate");
		String starttime=request.getParameter("starttime");
		String endtime=request.getParameter("endtime");
		String location=request.getParameter("location");
		String address=request.getParameter("address");
		String link=request.getParameter("link");
		String contact=request.getParameter("contact");
		String additionalinfo=request.getParameter("additionalinfo");
		//String file=request.getParameter("file");
		String partner=request.getParameter("partner");
		String eventtype=request.getParameter("eventtype");
		 String organizer = (String) request.getSession().getAttribute("userid");
	        if (organizer == null) {
	        	organizer = "";
	        }
	        
	        EventDao ud=new EventDao(DatabaseConnection.getConnection());
	        
	        
	    	
	        
	        ByteArrayInputStream inputStream = null;
	        Part filePart = request.getPart("file");
	        if (filePart != null) {
	            // prints out some information for debugging
//	            System.out.println(filePart.getName());
//	            System.out.println(filePart.getSize());
//	            System.out.println(filePart.getContentType());

	            //obtains input stream of the upload file
	            //the InputStream will point to a stream that contains
	            //the contents of the file
	            inputStream = (ByteArrayInputStream) filePart.getInputStream();
	        }
		
		Event event=new Event();
		
		DateTimeFormatter formatterdate = DateTimeFormatter.ofPattern("MM/dd/yyyy");
		DateTimeFormatter formattertime = DateTimeFormatter.ofPattern("HH:mm");
		  LocalDate lds = LocalDate.parse(startdate,formatterdate);
  	    LocalTime lts = LocalTime.parse(starttime,formattertime);
  	    LocalDateTime datetime1 = LocalDateTime.of(lds, lts);
  	    Timestamp timestamp1 = Timestamp.valueOf(datetime1);
  	  LocalDate lde = LocalDate.parse(enddate,formatterdate);
	    LocalTime lte = LocalTime.parse(endtime,formattertime);
	    LocalDateTime datetime2 = LocalDateTime.of(lde, lte);
	    Timestamp timestamp2 = Timestamp.valueOf(datetime2);
  	    event.setStart(timestamp1);
  	    event.setEnd(timestamp2);
  	  int returnVal=0;
  	  
  	returnVal=timestamp1.compareTo(timestamp2);
		 SimpleDateFormat d = new SimpleDateFormat("MM/dd/yyyy");
		 SimpleDateFormat t = new SimpleDateFormat("hh:mm");
		    try {
	        Date dt,dt2;
	        java.sql.Date sqlDate;
	        java.sql.Date sqlDate2;
	         
	        Date st = t.parse(starttime);
	        Date et = t.parse(endtime);
	        Time stime = new Time(st.getTime());
	        Time etime = new Time(et.getTime());
	    
	            dt = d.parse(startdate);
	            sqlDate = new java.sql.Date(dt.getTime());
	            event.setStartdate(sqlDate);
	            
	            dt2 = d.parse(enddate);
	            sqlDate2 = new java.sql.Date(dt2.getTime());
	            event.setEnddate(sqlDate2);

	            String available=ud.checkifDayIsAvailable(sqlDate,sqlDate2,partner, eventtype);
	            Timestamp current=new Timestamp(System.currentTimeMillis());
	            int timestampStatus = timestamp1.compareTo(current);  
	            if((returnVal>0)||(timestampStatus<=0)) {
	          	 	   request.getSession().setAttribute("eventsetup", "Please choose valid event duration!");
	          	 	System.out.println("Please choose valid event duration!");
	          	  response.sendRedirect("eventsetup.jsp");
	          	      }else if(available.equals("yes")) {
	            	
	            
	            	System.out.println(partner+"This person is available");
	            	 String timeavailability=ud.checkifTimeIsAvailable(sqlDate,sqlDate2,stime,etime, partner, eventtype);
		                if(timeavailability.equals("Available")) {
		                	System.out.println("This time slot is available");
		                	
		                	  event.setTitle(title);
		          	        event.setDescription(description);
		          	        event.setLocation(location);
		          	        event.setAddress(address);
		          	        event.setLink(link); 
		          	        event.setContact(contact);
		          	        event.setAdditionalinfo(additionalinfo);
		          	        event.setFile(inputStream);
		          	        event.setPartner(partner);
		          	        event.setEventtype(eventtype);
		          	        event.setOrganizer(organizer);
		          	        
		          	    
		          	        String eventStatus=ud.createEvent(event);
		          	        
		          	        if(eventStatus.equals("Created Event Successfully!")) {
		          	          request.getSession().setAttribute("bookings", "Created Event Successfully!");
		          	        response.sendRedirect("bookings.jsp");
		          	        }else {
		          	        	 request.getSession().setAttribute("eventsetup", eventStatus);
		          	        	 response.sendRedirect("eventsetup.jsp");
		          	        }
		                }else {
		                	System.out.println("This time slot is not available");
		                	 request.getSession().setAttribute("eventsetup", "This time slot is not available");
		                	 
		                	 response.sendRedirect("eventsetup.jsp");
		                }
	            }else {
	            	System.out.println(partner+"This person is not available");
	            	request.getSession().setAttribute("eventsetup", "This person is not available on this day");
               	 response.sendRedirect("eventsetup.jsp");
	            }
	   
		        event.setStarttime(stime);
		        event.setEndtime(etime);
	        } catch (ParseException ex) {
	            Logger.getLogger(EventSetupServlet.class.getName()).log(Level.SEVERE, null, ex);
	        }
		
	      
		
	}

}
