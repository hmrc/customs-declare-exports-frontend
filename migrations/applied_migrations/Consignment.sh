#!/bin/bash

echo "Applying migration Consignment"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /consignment               controllers.ConsignmentController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /consignment               controllers.ConsignmentController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeConsignment               controllers.ConsignmentController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeConsignment               controllers.ConsignmentController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "consignment.title = consignment" >> ../conf/messages.en
echo "consignment.heading = consignment" >> ../conf/messages.en
echo "consignment.consolidation = Consolidation" >> ../conf/messages.en
echo "consignment.singleShipment = Single shipment" >> ../conf/messages.en
echo "consignment.checkYourAnswersLabel = consignment" >> ../conf/messages.en
echo "consignment.error.required = Please give an answer for consignment" >> ../conf/messages.en

echo "Adding helper line into UserAnswers"
awk '/class/ {\
     print;\
     print "  def consignment: Option[Consignment] = cacheMap.getEntry[Consignment](ConsignmentId.toString)";\
     print "";\
     next }1' ../app/utils/UserAnswers.scala > tmp && mv tmp ../app/utils/UserAnswers.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def consignment: Option[AnswerRow] = userAnswers.consignment map {";\
     print "    x => AnswerRow(\"consignment.checkYourAnswersLabel\", s\"consignment.$x\", true, routes.ConsignmentController.onPageLoad(CheckMode).url)";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration Consignment completed"
