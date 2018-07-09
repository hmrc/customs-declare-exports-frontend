#!/bin/bash

echo "Applying migration SubmitPage"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /submitPage               controllers.SubmitPageController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /submitPage               controllers.SubmitPageController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeSubmitPage                        controllers.SubmitPageController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeSubmitPage                        controllers.SubmitPageController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "submitPage.title = submitPage" >> ../conf/messages.en
echo "submitPage.heading = submitPage" >> ../conf/messages.en
echo "submitPage.checkYourAnswersLabel = submitPage" >> ../conf/messages.en
echo "submitPage.error.required = Enter submitPage" >> ../conf/messages.en
echo "submitPage.error.length = SubmitPage must be 100 characters or less" >> ../conf/messages.en

echo "Adding helper line into UserAnswers"
awk '/class/ {\
     print;\
     print "  def submitPage: Option[String] = cacheMap.getEntry[String](SubmitPageId.toString)";\
     print "";\
     next }1' ../app/utils/UserAnswers.scala > tmp && mv tmp ../app/utils/UserAnswers.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def submitPage: Option[AnswerRow] = userAnswers.submitPage map {";\
     print "    x => AnswerRow(\"submitPage.checkYourAnswersLabel\", s\"$x\", false, routes.SubmitPageController.onPageLoad(CheckMode).url)";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration SubmitPage completed"
