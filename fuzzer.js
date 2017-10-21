var Random = require('random-js'),
	fs = require('fs'),
	stackTrace = require('stacktrace-parser'),
	proc = require('child_process')
    ;

var fuzzer = 
{
    random : new Random(Random.engines.mt19937().autoSeed()),
    
    seed: function (kernel)
    {
        fuzzer.random = new Random(Random.engines.mt19937().seed(kernel));
    },

    mutate:
    {
        string: function(val)
        {
            // MUTATE IMPLEMENTATION HERE
            var file = fs.readFileSync(val,'utf-8');


            var array = file.split('\n');

            //do {
            for(var i = 0; i < array.length; i++)
            {
            	var string = array[i];

            	if( fuzzer.random.bool(0.2) )
            	{
            		if(string.match("while") || string.match("if"))
	            	{
	            		if(string.match("<"))
	            		{
	            			string = string.replace("<",">");
	            		} else if(string.match(">"))
	            		{
	            			string = string.replace(">","<");
	            		}
	            	}
            	}

            	if( fuzzer.random.bool(0.3) )
            	{
            		if(string.match("==")) {
            			string = string.replace("==","!=");	
	            	}
	            	else if(string.match("!="))
	            	{
	            		string = string.replace("!=","==");
	            	}
            	}
            	
            	//mutuate existing strings into random strings
            	//make sure to not change any beans (line will have @)/final (line will have final)/important strings/dont mess up locale
            	if (fuzzer.random.bool(0.2) && !string.match("@") && !string.match("private") && !string.match("final") && !string.match("Locale"))
            	{
            		//will match for a value surronded by quotes, aka a string!
            		var mtch = string.match(/(["])(?:(?=(\\?))\2.)*?\1/);
            		if( mtch != null)
            		{
            			//create a new random string
            			var newString = fuzzer.random.string(mtch[0].length - 2);

            			//replace the string with random string, but make sure to maintain quotes around it
            			string = string.replace(mtch[0], '"' + newString + '"');
            		}
            	}
            	

            	//avoid anything with public, private, import, or package JUST IN CASE
                //also some exception have 0/1 in the variable name e1
            	// if(!string.match("public") && !string.match("private") && !string.match("import") && !string.match("package") && !string.match("e1"))
            	// {
            	// 	if (string.match("0"))
            	// 	{
            	// 		// half the time, replace first zero, other half replace all zeroes
            	// 		if (fuzzer.random.bool(0.5) )
            	// 		{
            	// 			string = string.replace("0","1");
            	// 		} else
            	// 			string = string.replace("0/g","1");
            			
            	// 	} else if (string.match("1"))
            	// 	{
            	// 		if (fuzzer.random.bool(0.5) )
            	// 		{
            	// 			string = string.replace("1","0");
            	// 		} else
            	// 			string = string.replace("1/g","0");
            	// 	}
            	// }

            	//set mutated string back
            	array[i] = string;
            }

            //Join the array of strings, and overwrite the file
            var newFile = array.join("\n");
            //console.log(newFile);
            fs.writeFileSync(val, newFile, {encoding:'utf8'});
        }
    }
};

// Get all the files recursively given a path
var walkSync = function(dir, filelist) {
    var path = path || require('path');
    var fs = fs || require('fs'),
    files = fs.readdirSync(dir);
    filelist = filelist || [];
    files.forEach(function(file) {
        if (fs.statSync(path.join(dir, file)).isDirectory()) {
            filelist = walkSync(path.join(dir, file), filelist);
        }
        else {
            filelist.push(path.join(dir, file));
        }
    });
    return filelist;
};

//there is a hook set up in jenkins to pull after every push for the branch fuzzer
function commitandrevert(iteration){
	// push mutated code to fuzzer branch, force it to overwrite previous mutated code, but give new commit
	proc.execSync('git add . && git commit -m "fuzzed code, iteration ' + iteration +'" && git push origin fuzzer --force');

	// now revert fuzzer branch to what the master branch has
	proc.execSync('git fetch origin && git reset --hard origin/master');
}

mutationTesting('iTrust/src/main/edu/ncsu/csc/itrust',1);

function mutationTesting(path,iterations)
{

	//First checkout fuzzer branch which we add mutated code too
	proc.execSync('git checkout fuzzer');

	//console.log(allFiles);
	//var filesnomodel = [];

	for(var i = 0; i < 1; i++)
	{
		var allFiles = walkSync(path);
		for(var k = 0; k < allFiles.length; k++)
		{
			var f = allFiles[k];
			
			// dont mutate models b/c they are very important AND also only mutuate .java files, not .properties or anything else
			if (!f.match("/model/") && f.match(".java") && !f.match("Util"))
			//if (f.match("properties")) 
			{
				//console.log(f);
				fuzzer.mutate.string(f);
			}
		}
		//push off fuzzed code
		commitandrevert(i)
	}
}