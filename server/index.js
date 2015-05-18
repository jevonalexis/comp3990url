
var mongoose = require('mongoose'),
	crypto = require('crypto'),
	db = mongoose.connection,
	Schema = mongoose.Schema,
	RoomSchema,  Room,
	express = require("express")
	bodyParser = require('body-parser'),
	session = require('express-session'),
	app = express(),
	cool = require('cool-ascii-faces');
	//port = 3000,
	KedLib = require("./lib");


app.set('port', (process.env.PORT || 3005));
app.use(express.static(__dirname + '/public'));


//Set up the listeners for the mongo database
db.on('error', function(err){
	console.log("Error during database Connection" + err);
});

db.once('open', function(){
	console.log("Database Successfully Connected");
	configureModels(); // We run operations within this event because we can guarantee that the connection is established

	//KedLib.clearRoom(Room); //Run if we want to delete all users

	checkCreateDefaultUser(); // Checks to ensure that there is at least the default user within the database
});

mongoose.connect('mongodb://heroku_app35009688:rpa7sc718l8p3v16vableurtf2@ds053597.mongolab.com:53597/heroku_app35009688');//note if the database does not exist it will created it automatically

function configureModels(){
	//Set up the values that are acceptable to the database
	RoomSchema = Schema({
		room : { type: String, index: { unique: true }},
		building : String, 
		faculty : String,
		picture     : Object,
		latitude : Number,
		longitude : Number
	});

	Room = mongoose.model('Room', RoomSchema);

	MarkerSchema = Schema({
		description : String, 
		latitude : Number,
		longitude : Number,
		landmark: String,
		path : Object
	});

	Marker = mongoose.model('Marker', MarkerSchema);

	//Set up the values that are acceptable to the database
	UserSchema = Schema({
		username : { type: String, index: { unique: true }},
		password : String, 
		salt : {type: String , default: KedLib.genRandomString(12)},	
	});

	// We set the behaviour before the save event occurs
	// This behaviour will ensure that all passwords are encrypted before saving
	UserSchema.pre('save', function(next){
		//Using "this" gives access to the current model calling the function
		var hpass = crypto
					.createHash('sha1')
					.update(this.password + this.salt)
					.digest('hex');
		this.password = hpass; //Assign the hashed password to the password field of the current model
  		next(); // run the next event (which usually will be the save)
	});

	// To improve our modularity (specifically to adhere to SRP) we will allow the model to retain the knowledge required to authenticate itself

	UserSchema.methods.authenticate = function(password, callback){
		// If based on the results of the authentication we will execute the callback with information that will inform the program of the its state (authentication passed or failed)

		var tempHash = crypto
					.createHash('sha1')
					.update(password + this.salt)
					.digest('hex');
		if (tempHash === this.password){
			if (callback){
				callback(this); //I can pass anything here (as I am creating my own api)
			}
			return true;
		}
		callback(null);
		return false;
	}

	// Associate the Schema we created to a collection(database)
	// And store the reference to the Collection in the User variable as a model.
	User = mongoose.model('User', UserSchema);
}


// ***** Configuring Express *****
app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());
app.use(session({secret:"sadf3234",saveUninitialized: true,resave: true }));
app.use(function(req, res, next) {
  res.header("Access-Control-Allow-Origin", "*");
  res.header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
  next();
});
//=====================================================================================================
function checkCreateDefaultUser(){
	// We are using this function to create a default user.
	//This make sense to actually access the system. (some installations require the user to actually set the credentials manually)

	User.find({}, function(err, data){
		if (err)return;
		if (data.length < 1){ //Collection is empty therefore we can create a default user
			console.log("No User Detected: Creating Default User");
			var u = new User({
				username : 'admin',
				password : 'password'
			});
			u.save(function(err, user){
				if (err)console.log("Error occured while saving user "+ err);
				else console.log("User saved Successfully");
				console.log(user); 
			});
		}
	});
}

function checkAuth(req, res, next){
	if(!req.session.user){ //If user not set in the session we not logged in
		//Display appropriate error along with appropriate error code
		res.status(401).send("You are not authorized to view this page");
	}else{
		//We are logged in so we run the next operation (which is what we defined in the method call)
		next();
	}
}
//login to an existing user
app.get('/login/:username/:password', function (req, res) {
	// We read the username and password from the body
	//Typically we will submit via a form, which will encapsulate the data in the body of the POST request
	var u=req.param('username');
	var p=req.param('password');
	var post={"username":u,"password":p};
	console.log(post);
	if (post.username && post.password ){ //check to ensure username is passed
		console.log("Username and password supplied");

		// Attempt to find the username that matches the information supplied
		User.find({"username": post.username}, function(err, users){
			if (err){
				console.log("error occurred: " + err);
				res.status(500).json({"status":"failure"});
			}else{
				if (users.length > 0){ // We found a user that matched the username
					console.log("Successfully retrieved user from database");
					var u = users[0]; //we know that the username will be unique
					u.authenticate(post.password, function(result){
						if (result){
							req.session.user = result;
							req.session.username=post.username;
							req.session.save(function(err){;
								res.json({"status":"success"})

							});
						}else{
							res.json({"status":"failure"});
						}
					})
				}else{ // No user with the username was found
					console.log("No Username found");
					res.json({"status":"failure"});
				}
			}
		});
	}else{
		res.json({"status":"failure"});
	}
});
//creates a new user
app.post("/signup", function(req, res){
	var post = req.body;
	if (post.username && post.password){
		

		var u = new User({
			username: post.username,
			password: post.password,
			AmtOfFiles : 0,
			Files : []
		});

		u.save(function(err){
			if (err){
				console.log("unable to save user" + err);
				res.status(500).json({"status":"failure"});
			}else{
				console.log("user saved Successfully");
				res.status(200).json({"status":"success"});
			}
		});
	}
});
//logout of session
app.get("/logout", checkAuth, function(req, res){
	req.session.user=false;
	res.json({"status":"success"});
});
//sends all users
app.get("/users",function(req,res){
	User.find({}, function(err, data){
		if (err)return;
		res.json(data);
	});
})
// saves a new marker
app.post("/saveMarker",function(req,res){
	var post = req.body;
			console.log(post);
				console.log("description:"+ post.description);
				console.log("latitude:" +post.latitude);
				console.log("longitude:"+ post.longitude);
				console.log("landmark:"+post.landmark);
				console.log("path:"+post.path);

				
			var u = new Marker({
				description: post.description,
				latitude: post.latitude,
				longitude: post.longitude,
				landmark:post.landmark,
				path:post.path
			});
			console.log(u['path']['A']);
			
			
		u.save(function(err){
			if (err){
				console.log("unable to save marker" + err);
				res.status(500).send("Unable to save marker");
			}else{
				console.log("marker saved Successfully");
				res.send("marker room");
			}
		});
	});
//sends all markers
app.get("/dataMarker",function(req,res){
	Marker.find({}, function(err, data){
		if (err)return;
		res.json(data);
	});


});
//saves a new room
app.post("/save",function(req,res){
	var post = req.body;

	Room.find({"room":post.room
				}, function(err, data){
		if (err){
			console.log("unable to save room" + err);
			return;
		}
		if (data.length === 0){ //room doesnt exist
			console.log("here");
			var u = new Room({
				room: post.room,
				building: post.building,
				faculty: post.faculty,
				picture: post.picture,
				latitude: post.latitude,
				longitude: post.longitude
				
			});

			u.save(function(err){
				if (err){
					console.log("unable to save room" + err);
					res.status(500).send("Unable to save room");
				}else{
					console.log("room saved Successfully");
					res.send("saved room");
				}
			});
				
		}
		else{
			res.send("room already exist");
		}
	});
});

//sends all markers
app.get("/dataMarker",function(req,res){
	Marker.find({}, function(err, data){
		if (err)return;
		res.json(data);
	});


});
//sends all rooms
app.get("/data",function(req,res){
	Room.find({}, function(err, data){
		if (err)return;
		res.json(data);
	});
});
//sends the room with name:"room"
app.get("/room/:key",function(req,res){
	var key= req.param('key');
	console.log(key);
	key=key.replace("%2", " ");
	Room.find({"room":key}, function(err, data){
		if (err){
			res.json(data);
			return;}
		res.json(data);
	});
});
//sends all the rooms from faculty:"key"
app.get("/faculty/:key",function(req,res){
	var key= req.param('key');
	console.log(key);
	key=key.replace("%2", " ");
	Room.find({"faculty":key}, function(err, data){
		if (err || data.length===0){
			res.json(data);
			return;}
		res.json(data);
	});
});
//sends all the rooms from building:"key"
app.get("/building/:key",function(req,res){
	var key= req.param('key');
	console.log(key);
	key=key.replace("%2", " ");
	Room.find({"building":key}, function(err, data){
		if (err || data.length===0){
			res.json(data);
			return;}
		res.json(data);
	});
});
//delete room with an id :"key"
app.get("/delete/:key",function(req,res){
	Room.find({"_id":req.param('key')}).remove(function(err){
		if (err)console.log(err);
		else res.send("item : "+req.param('key')+"deleted");
	});
});
//delete all rooms
app.get("/deleteall",function(req,res){
	Room.find({}).remove(function(err){
		if (err)console.log(err);
		else res.send("All items deleted");
	});
});

//delete a marker with name: "key"
app.get("/deletemarker/:key",function(req,res){
	Marker.find({"name":req.param('key')}).remove(function(err){
		if (err)console.log(err);
		else res.send("item : "+req.param('key')+"deleted");
	});
});

//delete all markers
app.get("/deletemarkerall",function(req,res){
	Marker.find({}).remove(function(err){
		if (err)console.log(err);
		else res.send("All items deleted");
	});
});

//best possible path
app.get("/path/:slat/:slong/:elat/:elong",function(req,res){

	var sLat = parseFloat(req.param('slat'));
	var sLong = parseFloat(req.param('slong'));
	var eLat = parseFloat(req.param('elat'));
	var eLong = parseFloat(req.param('elong'));

	Marker.find({},function(err,data){
		var endSmall,startSmall;
		endSmall = startSmall = Math.sqrt(Math.pow(sLat-eLat,2)+Math.pow(sLong-eLong,2));

		data.forEach(function(n){
			var Stemp=Math.sqrt(Math.pow(sLat-n.latitude,2)+Math.pow(sLong-n.longitude,2));
			var Etemp=Math.sqrt(Math.pow(eLat-n.latitude,2)+Math.pow(eLong-n.longitude,2));
			
			if(startSmall>Stemp){
				startSmall=Stemp;
				sNode=n;
			}
			if(endSmall>Etemp){
				endSmall=Etemp;
				eNode=n;
			}
		});
	var pathData=new Array();

	pathData.push(sNode);
		Marker.find({},function(err,data){
			var node;
			while(sNode.landmark!=eNode.landmark){
				data.forEach(function(el){
					if(el.landmark === sNode.path[eNode.landmark])
						node=el;
				});
					sNode=node;
					pathData.push(sNode);
			}
			res.json(pathData); 
		});
	});	
});


app.get("/", function(req, res){
console.log("[200] " + req.method + " to " + req.url);
  res.writeHead(200, "OK", {'Content-Type': 'text/html'});
  res.write('<html><head><title>Hello Noder!</title></head><body>');
  res.write('<h1>Welcome Noder, who are you?</h1>');
  res.write('<form action="/save" method="POST">');//url!!!
  res.write('Faculty: <input type="text" name="faculty" /><br />');
  res.write('Building: <input type="text" name="building" /><br />');
  res.write('Room: <input type="text" name="room" /><br />');
  res.write('Longitude: <input type="text" name="longitude" /><br />');
  res.write('Latitude: <input type="text" name="latitude" /><br />');
  res.write('Picture: <input type="text" name="picture" /><br />');
  res.write('<input type="submit" />');
  res.write('</form> ');

  	res.write('</body></html');
  res.end();


});

app.listen(app.get('port'), function() {
  console.log("Node app is running at localhost:" + app.get('port'));
});
