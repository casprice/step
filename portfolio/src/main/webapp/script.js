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

var map;
var mapKey = config.MAPS_API_KEY;
// Create the script tag, set the appropriate attributes
var script = document.createElement('script');
script.type= 'text/javascript';
script.src = 'https://maps.googleapis.com/maps/api/js?key=' + mapKey + '&callback=initMap';
script.defer = true;
script.async = true;
// Append the 'script' element to 'head'
document.head.appendChild(script);

const restaurants = [
  ['Tacos El Gordo', 32.629283, -117.089016],
  ['Vallarta Express Mexican Eatery', 32.916935, -117.124225],
  ['Lolita\'s Mexican Food', 32.832446, -117.160438],
  ['Tajima Ramen Convoy', 32.825659, -117.154433],
  ['Friend\'s House Korean', 32.825478, -117.154096],
  ['Rakiraki Ramen & Tsukemen', 32.824843, -117.155504],
  ['Koon Thai Kitchen', 32.814630, -117.153565],
  ['Kung Fu Tea', 32.830075, -117.152454],
  ['Happy Lemon Convoy', 32.824842, -117.154448],
  ['Somi Somi', 32.824393, -117.155277],
  ['Ding Tea Balboa', 32.819686, -117.177562],
  ['Wushiland Boba', 32.833550, -117.160382],
  ['Menya Ultra', 32.832285, -117.147365]
];

// Attach your callback function to the `window` object
window.initMap = function() {
  // Create a new StyledMapType object, passing it an array of styles,
  // and the name to be displayed on the map type control.
  var styledMapType = new google.maps.StyledMapType(
    [
      {elementType: 'geometry', stylers: [{color: '#ebe3cd'}]},
      {elementType: 'labels.text.fill', stylers: [{color: '#523735'}]},
      {elementType: 'labels.text.stroke', stylers: [{color: '#f5f1e6'}]},
      {
        featureType: 'administrative',
        elementType: 'geometry.stroke',
        stylers: [{color: '#c9b2a6'}]
      },
      {
        featureType: 'administrative.land_parcel',
        elementType: 'geometry.stroke',
        stylers: [{color: '#dcd2be'}]
      },
      {
        featureType: 'administrative.land_parcel',
        elementType: 'labels.text.fill',
        stylers: [{color: '#ae9e90'}]
      },
      {
        featureType: 'landscape.natural',
        elementType: 'geometry',
        stylers: [{color: '#dfd2ae'}]
      },
      {
        featureType: 'poi',
        elementType: 'geometry',
        stylers: [{color: '#dfd2ae'}]
      },
      {
        featureType: 'poi',
        elementType: 'labels.text.fill',
        stylers: [{color: '#93817c'}]
      },
      {
        featureType: 'poi.park',
        elementType: 'geometry.fill',
        stylers: [{color: '#a5b076'}]
      },
      {
        featureType: 'poi.park',
        elementType: 'labels.text.fill',
        stylers: [{color: '#447530'}]
      },
      {
        featureType: 'road',
        elementType: 'geometry',
        stylers: [{color: '#f5f1e6'}]
      },
      {
        featureType: 'road.arterial',
        elementType: 'geometry',
        stylers: [{color: '#fdfcf8'}]
      },
      {
        featureType: 'road.highway',
        elementType: 'geometry',
        stylers: [{color: '#f8c967'}]
      },
      {
        featureType: 'road.highway',
        elementType: 'geometry.stroke',
        stylers: [{color: '#e9bc62'}]
      },
      {
        featureType: 'road.highway.controlled_access',
        elementType: 'geometry',
        stylers: [{color: '#e98d58'}]
      },
      {
        featureType: 'road.highway.controlled_access',
        elementType: 'geometry.stroke',
        stylers: [{color: '#db8555'}]
      },
      {
        featureType: 'road.local',
        elementType: 'labels.text.fill',
        stylers: [{color: '#806b63'}]
      },
      {
        featureType: 'transit.line',
        elementType: 'geometry',
        stylers: [{color: '#dfd2ae'}]
      },
      {
        featureType: 'transit.line',
        elementType: 'labels.text.fill',
        stylers: [{color: '#8f7d77'}]
      },
      {
        featureType: 'transit.line',
        elementType: 'labels.text.stroke',
        stylers: [{color: '#ebe3cd'}]
      },
      {
        featureType: 'transit.station',
        elementType: 'geometry',
        stylers: [{color: '#dfd2ae'}]
      },
      {
        featureType: 'water',
        elementType: 'geometry.fill',
        stylers: [{color: '#b9d3c2'}]
      },
      {
        featureType: 'water',
        elementType: 'labels.text.fill',
        stylers: [{color: '#92998d'}]
      }
    ],
    {name: 'Styled Map'});

  map = new google.maps.Map(document.getElementById("map"), {
    center: { lat: 32.8328, lng: -117.2713 },
    zoom: 11,
    mapTypeId: 'styled_map'
  });

  //Associate the styled map with the MapTypeId and set it to display.
  map.mapTypes.set('styled_map', styledMapType);
  map.setMapTypeId('styled_map');

  setMarkers();
};

/**
 * Adds markers to the map.
 */
function setMarkers() {
  for (var i = 0; i < restaurants.length; i++) {
    var restaurant = restaurants[i];
    var marker = new google.maps.Marker({
      position: {lat: restaurant[1], lng: restaurant[2]},
      map: map,
      title: restaurant[0],
      zIndex: i + 1
    });
  }
}

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