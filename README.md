# jira-tracker

So this is simple lambda function which can be triggered by any of AWS event.
The main goal is to read all jira template files and automatically log issue worklogs.
If you are tired of this routine feel free to use :-)  

To create your own template file look into `template.json`.

<b>Note:</b> if on a day there is already 1 or more worklogs, then script won't create 
its worklog, day must be empty.
Also the issue have to exist!