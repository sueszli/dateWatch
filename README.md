# DateWatch
Make speed dating paperless!

Check out the figma prototype file in this repository or [take a look at the mockups here](https://www.figma.com/file/VfUssciRWQJCU6zIxfyD3h/DateWatch-(Read-Only))
to get a general overview of this apps functionality.

* Organizers and participants of speed dating events can register themselves and log in.
* Organizers can plan ahead and register the events they want to hold and get a link and short code that they can share their events with.
* Participants can look up events or use a short code to find them.
* Once at the event, participants can see the time they have left with their partner in the current speed dating round on their phone and can then tell whether they liked their partner.
(See screenshots below).
* After the event, the users get notified via an email that they can look at their matches on the app.
* [... and many more!]

<p float="left">
  <img src="https://user-images.githubusercontent.com/61852663/158476399-f38d6e1a-a615-4c87-9f36-ffe7eef44a3b.png" width="200">
  <img src="https://user-images.githubusercontent.com/61852663/158476483-423f6aec-e985-4a71-957e-486c673daa0a.png" width="210">
</p>
<br><br>

## About the Team
The development team consisted of 6 students and each contributed about 200-300 hours to this project.

## Technical Features
The used technology stack consists of:
- Java Spring Boot
- Angular, Bootstrap
- H2 Database

One challenge in designing this distributed system consisted of synchronizing the mobile users in real time and their different relationships.
To optimize the performance and synchronize the shared clock, Server Sent Events SSE were used in the backend.
