#!/bin/bash

echo "Applying migration EnterEORI"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /enterEORI               controllers.EnterEORIController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /enterEORI               controllers.EnterEORIController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeEnterEORI                        controllers.EnterEORIController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeEnterEORI                        controllers.EnterEORIController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "enterEORI.title = enterEORI" >> ../conf/messages.en
echo "enterEORI.heading = enterEORI" >> ../conf/messages.en
echo "enterEORI.checkYourAnswersLabel = enterEORI" >> ../conf/messages.en
echo "enterEORI.error.required = Enter enterEORI" >> ../conf/messages.en
echo "enterEORI.error.length = EnterEORI must be 100 characters or less" >> ../conf/messages.en

echo "Adding helper line into UserAnswers"
awk '/class/ {\
     print;\
     print "  def enterEORI: Option[String] = cacheMap.getEntry[String](EnterEORIId.toString)";\
     print "";\
     next }1' ../app/utils/UserAnswers.scala > tmp && mv tmp ../app/utils/UserAnswers.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def enterEORI: Option[AnswerRow] = userAnswers.enterEORI map {";\
     print "    x => AnswerRow(\"enterEORI.checkYourAnswersLabel\", s\"$x\", false, routes.EnterEORIController.onPageLoad(CheckMode).url)";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration EnterEORI completed"
