// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

var mapKey = config.MAPS_API_KEY;
// Create the script tag, set the appropriate attributes
var script = document.createElement('script');
script.type= 'text/javascript';
script.src = 'https://maps.googleapis.com/maps/api/js?key=' + mapKey + '&callback=initMap';
script.defer = true;
script.async = true;

// Attach your callback function to the `window` object
window.initMap = function() {
  map = new google.maps.Map(document.getElementById("map"), {
    center: { lat: 32.8328, lng: -117.2713 },
    zoom: 12
  });
};

// Append the 'script' element to 'head'
document.head.appendChild(script);

var map;

/** 
 * When the page loads, get from /login whether the user is logged in, and
 * update the Login/Logout url and display name accordingly. Then, get the
 * comments from the Datastore.
 */
function loadPage() {

  fetch('/get-account')
    .then(response => response.json())
    .then((loginCredentials) => {
      document.getElementById('new-comment-name').innerText = loginCredentials.nickname;
      document.getElementById('log-in-btn-link').setAttribute("href", loginCredentials.authenticationUrl);

      if (loginCredentials.isLoggedIn) {
        document.getElementById('log-in-btn-link').innerHTML = "Log out";
      } else {
        document.getElementById('log-in-btn-link').innerHTML = "Log in";
      }
  });

  getComments();
}

/**
 * Set element with ID enableID to be isEnabled.
 */
function setDisabled(enableID, isDisabled) {
  document.getElementById(enableID).disabled = isDisabled; 
}

/**
 * Enable the submit button if this element contains text.
 */
function checkEmptyField() {
  if(document.getElementById('text-input').value == '') { 
    document.getElementById('post-comment-btn').disabled = true;
  } else { 
    document.getElementById('post-comment-btn').disabled = false;
  }
}

/**
 * Fetches stats from the servers and adds them to the DOM.
 */
function getComments() {
  fetch('/list-comments?max-comments=' + document.getElementById("max-comments").value)
    .then(response => response.json())
    .then((comments) => {
      const commentsListElement = document.getElementById('comments-container');
      commentsListElement.innerHTML = "";

      comments.forEach((comment) => {
        commentsListElement.appendChild(createCommentElement(comment));
      });

      document.getElementById('num-comments-title').innerText = 
        comments.length + ' Comments';
  });
}

/** Creates an element that represents a comment. */
function createCommentElement(comment) {
  const commentElement = document.createElement('li');
  commentElement.className = 'comment-item';

  const userImageElement = document.createElement('img');
  userImageElement.src = 'images/User_photo.svg';

  const commentContentElement = document.createElement('div');
  commentContentElement.className = 'comment-item-content';

  const nameElement = document.createElement('span');
  nameElement.innerText = comment.name;
  nameElement.className = 'comment-name';

  const bodyElement = document.createElement('span');
  bodyElement.innerText = comment.body;

  commentContentElement.appendChild(nameElement);
  commentContentElement.appendChild(bodyElement);

  commentElement.appendChild(userImageElement);
  commentElement.appendChild(commentContentElement);
  
  return commentElement;
}

/** Tells the server to delete the comment */
function deleteComments() {
  fetch('/delete-comments', {method: 'POST', body: new URLSearchParams()})
    .then(response => response.text())
    .then(() => {
      getComments();
  });
}