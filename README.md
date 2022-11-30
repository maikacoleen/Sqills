# README - Group 36 - 2019

### Trello
Our project board can be found within the following link: https://trello.com/b/qDrVL2yk

### Technical Aspects:
- Google Calendar is the main source from where the server and database read/write their data. All reservations made whether it by the web application, administration page or Google Calendar, it will be sent to the Google Calendar first. Then it updates the others accordingly. With this design, duplications/overlapping are avoided. If there is a discrepancy in the reservation process, the event creation will be rejected. Per rejection, Google Calendar sends an email to the reserver notifying the declination. 
- Another aspect we are proud of is the structure of the java, especially the design pattern.


### Test Run:
- The application isn't running online at the moment, so to run it download the source code.
- Then import the project into eclipse (or other program), add the application to a local Tomcat server and run it in your browser with the following link: http://localhost:8080/sqills/
- This will bring you to the 'Overview’ page. Here you can see an overview of all the rooms and a live view of their statuses ranging from ‘Available’, ‘Occupied’, ‘Available Soon’, and ‘Occupied Soon’.
- To make sure the app displays the right room at all times, we implemented the option to set a default room. This means that, at any point in time, the page will automatically be redirected
  back to this default page after 3 minutes of no user action.
- If you want to get access to the ‘Administrator’ page, click the logo on the top left of the ‘Overview’ page. A popup will come up asking if you want to be redirected. Click ‘Yes’. To login, use the following credentials:
	- Username: admin
	- Password: admin
- An administrator has full power over all reservations. He/she can add, edit and delete all reservations.
- From the ‘Overview’ page, one can be directed to one of the ‘Room’ pages by clicking on desired room. The ‘Room’ page should be a very clear page where one can immediately see the status of the room.
- On the ‘Room’ page, with the button on the bottom left, you can make a new reservation. The bottom middle button slides out a ‘Room Schedule’ which you can see all upcoming reservations for the room. The button on the bottom right takes you back to the ‘Overview’ page.

### Notes:
- The application is developed with a tablet’s landscape mode in mind, however it is also possible to use it in portrait mode. It is recommended to run the web application with the iPad view built within Google Chrome.

