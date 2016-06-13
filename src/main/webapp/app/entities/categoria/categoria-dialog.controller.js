(function() {
    'use strict';

    angular
        .module('ecofruitApp')
        .controller('CategoriaDialogController', CategoriaDialogController);

    CategoriaDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'DataUtils', 'entity', 'Categoria', 'Subcategoria'];

    function CategoriaDialogController ($timeout, $scope, $stateParams, $uibModalInstance, DataUtils, entity, Categoria, Subcategoria) {
        var vm = this;

        vm.categoria = entity;
        vm.clear = clear;
        vm.byteSize = DataUtils.byteSize;
        vm.openFile = DataUtils.openFile;
        vm.save = save;
        vm.subcategorias = Subcategoria.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.categoria.id !== null) {
                Categoria.update(vm.categoria, onSaveSuccess, onSaveError);
            } else {
                Categoria.save(vm.categoria, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ecofruitApp:categoriaUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
