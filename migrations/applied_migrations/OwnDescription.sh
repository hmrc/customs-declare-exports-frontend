#!/bin/bash

echo "Applying migration OwnDescription"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /ownDescription                       controllers.OwnDescriptionController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /ownDescription                       controllers.OwnDescriptionController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeOwnDescription                       controllers.OwnDescriptionController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeOwnDescription                       controllers.OwnDescriptionController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "ownDescription.title = ownDescription" >> ../conf/messages.en
echo "ownDescription.heading = ownDescription" >> ../conf/messages.en
echo "ownDescription.checkYourAnswersLabel = ownDescription" >> ../conf/messages.en
echo "ownDescription.error.required = Please give an answer for ownDescription" >> ../conf/messages.en

echo "Adding helper line into UserAnswers"
awk '/class/ {\
     print;\
     print "  def ownDescription: Option[Boolean] = cacheMap.getEntry[Boolean](OwnDescriptionId.toString)";\
     print "";\
     next }1' ../app/utils/UserAnswers.scala > tmp && mv tmp ../app/utils/UserAnswers.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def ownDescription: Option[AnswerRow] = userAnswers.ownDescription map {";\
     print "    x => AnswerRow(\"ownDescription.checkYourAnswersLabel\", if(x) \"site.yes\" else \"site.no\", true, routes.OwnDescriptionController.onPageLoad(CheckMode).url)"; print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration OwnDescription completed"
