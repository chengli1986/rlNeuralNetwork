# RLBot
##Reinforcement Learning Robocode

###Run robocode in IDE 

Program arguments: -battle battles\\592.battle -tps 32 -nodisplay
VM arguments: -Xmx512M -Dsun.io.useCanonCaches=false -Ddebug=false

Note that setting debug flag off eliminates stuck issue

config/robocode.properties - the property file contains robocode settings in GUI -> Preference dialogue, chaning this file and run the code will take effect

robots/robocode.database - I sitll don't quite understand what this file is for but deleting this file makes robots stupid...

###Useful reference links:

Robocode Wiki: http://robowiki.net/wiki/Main_Page

Q-Learning: http://www.pudn.com/downloads196/sourcecode/java/detail923236.html

Anti-Gravity Tutorial: http://www.ibm.com/developerworks/library/j-antigrav/

Anti-Gravity Sample Code: https://code.google.com/p/ics-robocode-example/source/browse/trunk/src/edu/hawaii/ics413/philipmjohnson/antigravitybot/AntiGravityBot.java

###Robocode Usages:

	Usage: robocode [-?] [-help] [-cwd path] [-battle filename [-results filename]
        	        [-record filename] [-recordXML filename] [-replay filename]
                	[-tps tps] [-minimize] [-nodisplay] [-nosound]

	where options include:
  	-? or -help                Prints out the command line usage of Robocode
  	-cwd <path>                Change the current working directory
  	-battle <battle file>      Run the battle specified in a battle file
  	-results <results file>    Save results to the specified text file
  	-record <bin record file>  Record the battle into the specified file as binary
  	-recordXML <xml rec file>  Record the battle into the specified file as XML
  	-replay <record file>      Replay the specified battle record
  	-tps <tps>                 Set the TPS > 0 (Turns Per Second)
  	-minimize                  Run minimized when Robocode starts
  	-nodisplay                 Run with the display / GUI disabled
  	-nosound                   Run with sound disabled

	Java Properties include:

  	-DWORKINGDIRECTORY=<path>  Set the working directory
  	-DROBOTPATH=<path>         Set the robots directory (default is 'robots')
  	-DBATTLEPATH=<path>        Set the battles directory (default is 'battles')
  	-DNOSECURITY=true|false    Enable/disable Robocode's security manager
  	-Ddebug=true|false         Enable/disable debugging used for preventing
                             robot timeouts and skipped turns, and allows an
                             an unlimited painting buffer when debugging robots
  	-DlogMessages=true|false   Log messages and warnings will be disabled
  	-DlogErrors=true|false     Log errors will be disabled
  	-DEXPERIMENTAL=true|false  Enable/disable access to peer in robot interfaces
  	-DPARALLEL=true|false      Enable/disable parallel processing of robots turns
  	-DRANDOMSEED=<long number> Set seed for deterministic behavior of random
                             numbers


