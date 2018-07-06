#!/bin/bash

echo "Applying migration Dashboard"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /dashboard                       controllers.DashboardController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "dashboard.title = dashboard" >> ../conf/messages.en
echo "dashboard.heading = dashboard" >> ../conf/messages.en

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration Dashboard completed"
