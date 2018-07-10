#!/bin/bash

echo "Applying migration HaveRepresentative"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /haveRepresentative               controllers.HaveRepresentativeController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /haveRepresentative               controllers.HaveRepresentativeController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeHaveRepresentative               controllers.HaveRepresentativeController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeHaveRepresentative               controllers.HaveRepresentativeController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "haveRepresentative.title = haveRepresentative" >> ../conf/messages.en
echo "haveRepresentative.heading = haveRepresentative" >> ../conf/messages.en
echo "haveRepresentative.yes = Yes" >> ../conf/messages.en
echo "haveRepresentative.no = No" >> ../conf/messages.en
echo "haveRepresentative.checkYourAnswersLabel = haveRepresentative" >> ../conf/messages.en
echo "haveRepresentative.error.required = Please give an answer for haveRepresentative" >> ../conf/messages.en

echo "Adding helper line into UserAnswers"
awk '/class/ {\
     print;\
     print "  def haveRepresentative: Option[HaveRepresentative] = cacheMap.getEntry[HaveRepresentative](HaveRepresentativeId.toString)";\
     print "";\
     next }1' ../app/utils/UserAnswers.scala > tmp && mv tmp ../app/utils/UserAnswers.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def haveRepresentative: Option[AnswerRow] = userAnswers.haveRepresentative map {";\
     print "    x => AnswerRow(\"haveRepresentative.checkYourAnswersLabel\", s\"haveRepresentative.$x\", true, routes.HaveRepresentativeController.onPageLoad(CheckMode).url)";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration HaveRepresentative completed"
