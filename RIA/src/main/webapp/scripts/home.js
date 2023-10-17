(function () {

    let user;
    let personalMessage;
    let invitedList;
    let createdMeetingsList;
    let wizard;
    let modal;

    let pageOrchestrator = new PageOrchestrator();

    window.addEventListener("load", () => {
        if (sessionStorage.getItem("user") == null) {
            window.location.href = "index.html";
        } else {
            pageOrchestrator.start(); // initialize the components
            pageOrchestrator.refresh();
        }

    }, false);

    function InvitedAtList(_alert, _listcontainer, _listcontainerbody) {

        this.alert = _alert;
        this.listcontainer = _listcontainer;
        this.listcontainerbody = _listcontainerbody;

        this.show = function () {
            let self = this;
            makeCall("GET", "GetOtherMeetings", null,
                function (req) {
                    if (req.readyState === 4) {
                        const message = req.responseText;
                        if (req.status === 200) {
                            const invitedAtMeetings = JSON.parse(req.responseText);
                            if (invitedAtMeetings.length === 0) {
                                self.alert.textContent = "You haven't been invited to any meeting yet";
                                return;
                            }
                            self.update(invitedAtMeetings); // self visible by closure
                        }
                    }
                }
            );
        };

        this.update = function (arrayMeetings) {
            let row, titleCell, dateCell, timeCell, durationCell, maxCell;
            this.listcontainerbody.innerHTML = "";
            const self = this;
            arrayMeetings.forEach(function(meeting) {
                row = document.createElement("tr");
                titleCell = document.createElement("td");
                titleCell.textContent = meeting.title;
                row.appendChild(titleCell);

                dateCell = document.createElement("td");
                dateCell.textContent = meeting.date;
                row.appendChild(dateCell);

                timeCell = document.createElement("td");
                timeCell.textContent = meeting.time;
                row.appendChild(timeCell);

                durationCell = document.createElement("td");
                durationCell.textContent = meeting.length;
                row.appendChild(durationCell);

                maxCell = document.createElement("td");
                maxCell.textContent = meeting.numberOfParticipants;
                row.appendChild(maxCell);

                self.listcontainerbody.appendChild(row);
            });
            this.listcontainer.style.visibility = "visible";
        }
    }

    function CreatedList(_alert, _listcontainer, _listcontainerbody) {

        this.alert = _alert;
        this.listcontainer = _listcontainer;
        this.listcontainerbody = _listcontainerbody;

        this.show = function () {
            let self = this;
            makeCall("GET", "GetMyMeetings", null,
                function (req) {
                    if (req.readyState === 4) {
                        const message = req.responseText;
                        if (req.status === 200) {
                            const invitedAtMeetings = JSON.parse(req.responseText);
                            if (invitedAtMeetings.length === 0) {
                                self.alert.textContent = "You have not yet created a meeting.";
                                return;
                            }
                            self.update(invitedAtMeetings); // self visible by closure

                        }
                    }
                }
            );
        };

        this.update = function (arrayMeetings) {
            let row, titleCell, dateCell, timeCell, durationCell, maxCell;
            this.listcontainerbody.innerHTML = "";
            const self = this;
            self.alert.textContent = "";
            arrayMeetings.forEach(function(meeting) {
                row = document.createElement("tr");
                titleCell = document.createElement("td");
                titleCell.textContent = meeting.title;
                row.appendChild(titleCell);

                dateCell = document.createElement("td");
                dateCell.textContent = meeting.date;
                row.appendChild(dateCell);

                timeCell = document.createElement("td");
                timeCell.textContent = meeting.time;
                row.appendChild(timeCell);

                durationCell = document.createElement("td");
                durationCell.textContent = meeting.length;
                row.appendChild(durationCell);

                maxCell = document.createElement("td");
                maxCell.textContent = meeting.numberOfParticipants;
                row.appendChild(maxCell);

                self.listcontainerbody.appendChild(row);
            });
            this.listcontainer.style.visibility = "visible";
        }
    }

    function User(_user) {
        user = JSON.parse(_user);
        return user;
    }

    function PersonalMessage(messageContainer) {
        this.show = function(user) {
            messageContainer.textContent = user.name + ' ' + user.surname;
        }
    }

    function Wizard(wizardId, alert) {
        this.wizard = wizardId;
        this.alert = alert;

        this.registerEvents = function (orchestrator) {
            let self = this
            this.wizard.querySelector("input[type='button'].submit").addEventListener("click", (e) => {
                const form = e.target.closest("form");
                if (form.checkValidity()) {
                    makeCall("POST", 'CheckMeetingParameters', form,
                        function (req) {
                            if (req.readyState === XMLHttpRequest.DONE) {
                                const message = req.responseText;
                                if (req.status === 200) {
                                    sessionStorage.setItem("meetingInfo", req.responseText);
                                    modal.show();
                                } else {
                                    self.alert.textContent = message;
                                }
                            }
                        }
                    );
            } else {
                    form.reportValidity();
                }
            })

        }
    }

    function Modal(modal, list, invited, closeButton, selectedUsers, errorModalMessage) {
        this.modal = modal;
        this.users = list;
        this.invited = invited;
        this.closeButton = closeButton;
        this.selectedUsers = selectedUsers;
        this.alert = errorModalMessage;

        this.show = function () {
            modal.classList.remove("hidden");
            let self = this;
            makeCall("GET", "GetUsersList", null,
                function (req) {
                    if (req.readyState === 4) {
                        const message = req.responseText;
                        if (req.status === 200) {
                            let users = JSON.parse(req.responseText);
                            users = users.filter(self.delete);
                            if (users.length === 0) {
                                self.alert.textContent += "No users available";
                                return;
                            }
                            self.update(users); // self visible by closure
                            sessionStorage.setItem("availableUsers", req.responseText);
                        }
                    }
                }
            );
        };

        this.delete = function (u) {
            return u.idUser !== user.idUser;
        }

        this.update = function (users) {
            let row, label, checkbox;
            this.invited.innerHTML = "";
            const self = this;

            const template = document.querySelector("#id_invite_user");

            users.forEach(function(user) {
                row = template.content.cloneNode(true);
                label = row.querySelector("label");
                label.textContent = user.username;
                label.setAttribute("for", user.username);
                checkbox = row.querySelector("input");
                checkbox.setAttribute("field", user.username);
                checkbox.setAttribute("name", user.username);
                checkbox.setAttribute("value", user.username);

                self.invited.appendChild(row);
            });
            this.invited.style.visibility = "visible";
        }

        this.getCheckedUsers = function (selectedUsers){
            const checkedUsernames = [];
            for (let i = 0; i < selectedUsers.length; i++) {
                if (selectedUsers[i].checked) {
                    let username = selectedUsers[i].getAttribute("value")
                    checkedUsernames.push(username);
                }
            }
            return checkedUsernames;
        }
        
        this.increaseInvitationAttempts = function () {
            let self = this;
            makeCall("POST", "IncrementAttempts", null,
                function (req) {
                    if (req.readyState === 4) {
                        const message = req.responseText;
                        if (req.status === 200) {
                            sessionStorage.setItem("attempts", req.responseText);
                        } else {
                            self.alert.textContent = "You have used all three attempts to create a meeting";
                            self.invited.innerHTML = "";
                        }
                    }
                }
            );
        }

        this.registerEvents = function (orchestrator) {
            let self = this;
            this.modal.querySelector("input[type='button']").addEventListener("click", (e) => {
                const form = e.target.closest("form");
                const userSelected = this.getCheckedUsers(this.selectedUsers);
                const meeting = JSON.parse(sessionStorage.getItem("meetingInfo"));
                const formToSend = this.input(userSelected,meeting);
                if (userSelected.length <= 0) {
                    this.alert.textContent = "Please select at least one user";
                    this.alert.style.display = "block";

                } else if (userSelected.length > meeting.numberOfParticipants ) {
                    this.increaseInvitationAttempts();
                    let numberDeselect = userSelected.length - meeting.numberOfParticipants;
                    this.alert.textContent = "Too many user selected. Deselect at least " + numberDeselect + ((numberDeselect > 1) ? " users." : " user.");
                    this.alert.style.display = "block";

                } else {

                    if (form.checkValidity()) {
                        makeCall("POST", 'CreateMeeting', formToSend,
                            function (req) {
                                if (req.readyState === XMLHttpRequest.DONE) {
                                    const message = req.responseText;
                                    if (req.status === 200) {
                                        self.alert.textContent = "";
                                        self.closeAfterDone();
                                        orchestrator.refresh();
                                    } else {
                                        self.alert.textContent = message;
                                    }
                                }
                            }
                        );
                    } else {
                        form.reportValidity();
                    }
                }
            })

        }

        this.input = function (userSelected, meeting) {
            const formToSend = new FormData();
            let usersJson = JSON.stringify(userSelected);
            let meetingJson = JSON.stringify(meeting);
            formToSend.append('userSelected', usersJson);
            formToSend.append('meeting',meetingJson)
            return formToSend;
        }

        this.close = function (orchestrator) {
            let self = this;
            this.closeButton.addEventListener("click", (e) => {
                sessionStorage.removeItem("meetingInfo");
                self.alert.textContent = "";
                modal.classList.add("hidden");
            })
        }

        this.closeAfterDone = function () {
            sessionStorage.removeItem("meetingInfo");
            modal.classList.add("hidden");
        }

    }

    function PageOrchestrator() {

        this.start = function() {
            user = new User(
                sessionStorage.getItem("user")
            );

            personalMessage = new PersonalMessage(
                document.getElementById("id_user"));
            personalMessage.show(user);

            invitedList = new InvitedAtList(
                document.getElementById("id_alert_otherMeetings"),
                document.getElementById("id_other_meetings"),
                document.getElementById("id_otherListContainerBody"));
            invitedList.show();

            createdMeetingsList = new CreatedList(
                document.getElementById("id_alert_myMeetings"),
                document.getElementById("id_my_meetings"),
                document.getElementById("id_myListContainerBody"));
            createdMeetingsList.show();

            wizard = new Wizard(
                document.getElementById("id_createMeetingForm"),
                document.getElementById("errorMessage"));
            wizard.registerEvents(this);

            modal = new Modal(
                document.querySelector(".modal"),
                document.getElementById("id_chooseParticipants"),
                document.getElementById("id_invitation_list"),
                document.querySelector(".close"),
                document.getElementsByClassName("selected_user"),
                document.getElementById("error-modal-message"));
            modal.registerEvents(this);
            modal.close(this);

            document.querySelector("a[href='Logout']").addEventListener('click', () => {
                window.sessionStorage.removeItem('user');
                window.location.href = "index.html";
            })

        }

        this.refresh = function (){
            invitedList.show()
            createdMeetingsList.show()
        }
    }
})();