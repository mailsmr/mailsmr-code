#!/bin/bash
docker pull postgres:13
docker stop mewec-db
docker run --restart=unless-stopped --name mewec-db -p 7000:5432 -e POSTGRES_PASSWORD=mysecretpassword -e POSTGRES_USER=mewec --detach postgres:13

