HOW TO INSTALL AND RUN THE PROJECT LOCALLY:

1) Create a new folder on your computer for the project. Clone the repository using the command:

    git clone https://github.com/Educationalmoose/CMSC-495-Capstone-Project


2) Open the project in your IDE. I'm using Visual Studio Code since we're using Python and Java, but you can do whatever works best for you.


3) Create the virtual environment by opening the terminal in your IDE and using the command:

    WINDOWS: python -m venv venv
    MAC/LINUX: python3 -m venv venv


4) Activate the virtual environement using the command:

    WINDOWS: .\venv\Scripts\activate
    MAC/LINUX: source venv/bin/activate

        Now you should see a (venv) at the beginning of your terminal line. If you don't then make sure that you did these commands using command prompt and not powershell


5) Install the libraries using the command:

    pip install -r requirements.txt


6) Run the server using the command:

    python main.py

        If the server didn't run correctly, then try the following commands:

        set FLASK_APP=main.py
        flask run

7) To test the python demo, open a new terminal in the folder's path and type the following command:

    python fake_client.py

        You can experiment by changing the values in the matrix and seeing what the result is each time you run it.

8) To test the java demo, you can either compile the java file and run it in the console, or just use the run button inside the IDE.

    To compile:

        javac FakeClient.java
        java FakeClient

    If java doesn't recognize the Gson jar file, then try locating your IDE's external library importer and manually add the jar as an external library. You will have it downloaded in your /lib folder already


