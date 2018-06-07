#!/bin/bash

echo "Applying migration SelectRole"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /selectRole               controllers.SelectRoleController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /selectRole               controllers.SelectRoleController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeSelectRole               controllers.SelectRoleController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeSelectRole               controllers.SelectRoleController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "selectRole.title = Select role" >> ../conf/messages.en
echo "selectRole.heading = Select role" >> ../conf/messages.en
echo "selectRole.webLoaderArrivingGoods = Web loader arriving goods" >> ../conf/messages.en
echo "selectRole.webLoaderDepartingGoods = Web loader departing goods" >> ../conf/messages.en
echo "selectRole.checkYourAnswersLabel = Select role" >> ../conf/messages.en
echo "selectRole.error.required = Please give an answer for selectRole" >> ../conf/messages.en

echo "Adding helper line into UserAnswers"
awk '/class/ {\
     print;\
     print "  def selectRole: Option[SelectRole] = cacheMap.getEntry[SelectRole](SelectRoleId.toString)";\
     print "";\
     next }1' ../app/utils/UserAnswers.scala > tmp && mv tmp ../app/utils/UserAnswers.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def selectRole: Option[AnswerRow] = userAnswers.selectRole map {";\
     print "    x => AnswerRow(\"selectRole.checkYourAnswersLabel\", s\"selectRole.$x\", true, routes.SelectRoleController.onPageLoad(CheckMode).url)";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration SelectRole completed"
