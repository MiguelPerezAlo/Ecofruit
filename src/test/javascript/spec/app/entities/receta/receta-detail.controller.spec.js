'use strict';

describe('Controller Tests', function() {

    describe('Receta Management Detail Controller', function() {
        var $scope, $rootScope;
        var MockEntity, MockReceta, MockComentario, MockUser;
        var createController;

        beforeEach(inject(function($injector) {
            $rootScope = $injector.get('$rootScope');
            $scope = $rootScope.$new();
            MockEntity = jasmine.createSpy('MockEntity');
            MockReceta = jasmine.createSpy('MockReceta');
            MockComentario = jasmine.createSpy('MockComentario');
            MockUser = jasmine.createSpy('MockUser');
            

            var locals = {
                '$scope': $scope,
                '$rootScope': $rootScope,
                'entity': MockEntity ,
                'Receta': MockReceta,
                'Comentario': MockComentario,
                'User': MockUser
            };
            createController = function() {
                $injector.get('$controller')("RecetaDetailController", locals);
            };
        }));


        describe('Root Scope Listening', function() {
            it('Unregisters root scope listener upon scope destruction', function() {
                var eventType = 'ecofruitApp:recetaUpdate';

                createController();
                expect($rootScope.$$listenerCount[eventType]).toEqual(1);

                $scope.$destroy();
                expect($rootScope.$$listenerCount[eventType]).toBeUndefined();
            });
        });
    });

});
