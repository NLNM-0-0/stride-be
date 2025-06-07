#!/bin/bash

mongosh <<EOF
rs.initiate({
  _id: "rs0",
  members: [{ _id: 0, host: "mongo1:27017" }]
})

db.getSiblingDB("admin").createUser({
  user: "root",
  pwd: "123456",
  roles: [ { role: "root", db: "admin" } ]
})
EOF
