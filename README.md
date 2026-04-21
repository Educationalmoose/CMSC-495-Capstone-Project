HOW TO INSTALL AND RUN THE PROJECT LOCALLY:

1) Create a new folder on your computer for the project. Clone the repository using the command:

        git clone https://github.com/Educationalmoose/CMSC-495-Capstone-Project


2) Open the project in your IDE. I'm using Visual Studio Code since we're using Python and Java, but you can do whatever works best for you.


3) Create the virtual environment by opening the terminal in your IDE and using the command:

    WINDOWS:

        python -m venv venv

    MAC/LINUX:

        python3 -m venv venv


4) Activate the virtual environement using the command (in command prompt, not powershell):

    WINDOWS:

        .\venv\Scripts\activate

    MAC/LINUX:

        source venv/bin/activate

    Now you should see a (venv) at the beginning of your terminal line.


5) Install the libraries using the command:

        pip install -r requirements.txt


6) To test the java demo, you can either compile the java file and run it in the console, or just use the run button inside the IDE.

    To compile:

        javac DrawingApp.java
        java DrawingApp

7) If you install any external libraries in java or python, make sure you run this command before posting to github

        pip freeze > requirements.txt
    
    This will update the requirements list that we will need to be synced all the time to run smoothly.



