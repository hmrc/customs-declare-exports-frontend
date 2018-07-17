#!/bin/bash

echo "Applying migration NameAndAddress"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /nameAndAddress               controllers.NameAndAddressController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /nameAndAddress               controllers.NameAndAddressController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeNameAndAddress                        controllers.NameAndAddressController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeNameAndAddress                        controllers.NameAndAddressController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "nameAndAddress.title = nameAndAddress" >> ../conf/messages.en
echo "nameAndAddress.heading = nameAndAddress" >> ../conf/messages.en
echo "nameAndAddress.checkYourAnswersLabel = nameAndAddress" >> ../conf/messages.en
echo "nameAndAddress.error.required = Enter nameAndAddress" >> ../conf/messages.en
echo "nameAndAddress.error.length = NameAndAddress must be 100 characters or less" >> ../conf/messages.en

echo "Adding helper line into UserAnswers"
awk '/class/ {\
     print;\
     print "  def nameAndAddress: Option[String] = cacheMap.getEntry[String](NameAndAddressId.toString)";\
     print "";\
     next }1' ../app/utils/UserAnswers.scala > tmp && mv tmp ../app/utils/UserAnswers.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def nameAndAddress: Option[AnswerRow] = userAnswers.nameAndAddress map {";\
     print "    x => AnswerRow(\"nameAndAddress.checkYourAnswersLabel\", s\"$x\", false, routes.NameAndAddressController.onPageLoad(CheckMode).url)";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration NameAndAddress completed"
