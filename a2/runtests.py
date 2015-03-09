import socket
import os
import subprocess


# GO BACK N
local_host = socket.gethostbyname(socket.gethostname())
local_port = 6666

remote_host = "127.0.0.1"
remote_port = 6667

files = ["test_long.txt", "test_short.txt", "blue.png"]
RNS = [0, 5, 100]
WINDOWS = [10, 40, 80]

go_back_n_command = "java GoBackNSender {receiver} {receiver_port} {sender_port} ../../{file} {rn} {window}"
receiver_command = "java Receiver {sender} {sender_port} {receiver_port} ../../{file}"
file_format = "{rn}_{win}{name}"

os.system("cd src")
# os.system("javac *.java")
for file in files:
    for window in WINDOWS:
        for rn in RNS:
            send_command = go_back_n_command.format(receiver=remote_host, receiver_port=remote_port,
                                                    sender_port=local_port, file=file, rn=rn, window=window)
            new_file = file_format.format(rn=rn, win=window, name=file)
            receive_command = receiver_command.format(sender=local_host, receiver_port=remote_port,
                                                      sender_port=local_port, file=new_file)

            # subprocess.call(receive_command.split())
            # subprocess.call(send_command.split())

            print(receive_command)
            print(send_command)
            print()












