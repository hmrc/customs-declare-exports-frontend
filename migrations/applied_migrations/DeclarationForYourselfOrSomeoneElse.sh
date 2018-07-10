#!/bin/bash

echo "Applying migration DeclarationForYourselfOrSomeoneElse"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /declarationForYourselfOrSomeoneElse               controllers.DeclarationForYourselfOrSomeoneElseController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /declarationForYourselfOrSomeoneElse               controllers.DeclarationForYourselfOrSomeoneElseController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeDeclarationForYourselfOrSomeoneElse               controllers.DeclarationForYourselfOrSomeoneElseController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeDeclarationForYourselfOrSomeoneElse               controllers.DeclarationForYourselfOrSomeoneElseController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "declarationForYourselfOrSomeoneElse.title = declarationForYourselfOrSomeoneElse" >> ../conf/messages.en
echo "declarationForYourselfOrSomeoneElse.heading = declarationForYourselfOrSomeoneElse" >> ../conf/messages.en
echo "declarationForYourselfOrSomeoneElse.yourself = Yourself" >> ../conf/messages.en
echo "declarationForYourselfOrSomeoneElse.someoneElse = Someone Else" >> ../conf/messages.en
echo "declarationForYourselfOrSomeoneElse.checkYourAnswersLabel = declarationForYourselfOrSomeoneElse" >> ../conf/messages.en
echo "declarationForYourselfOrSomeoneElse.error.required = Please give an answer for declarationForYourselfOrSomeoneElse" >> ../conf/messages.en

echo "Adding helper line into UserAnswers"
awk '/class/ {\
     print;\
     print "  def declarationForYourselfOrSomeoneElse: Option[DeclarationForYourselfOrSomeoneElse] = cacheMap.getEntry[DeclarationForYourselfOrSomeoneElse](DeclarationForYourselfOrSomeoneElseId.toString)";\
     print "";\
     next }1' ../app/utils/UserAnswers.scala > tmp && mv tmp ../app/utils/UserAnswers.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def declarationForYourselfOrSomeoneElse: Option[AnswerRow] = userAnswers.declarationForYourselfOrSomeoneElse map {";\
     print "    x => AnswerRow(\"declarationForYourselfOrSomeoneElse.checkYourAnswersLabel\", s\"declarationForYourselfOrSomeoneElse.$x\", true, routes.DeclarationForYourselfOrSomeoneElseController.onPageLoad(CheckMode).url)";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration DeclarationForYourselfOrSomeoneElse completed"
