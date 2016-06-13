(function() {
    'use strict';

    angular
        .module('ecofruitApp')
        .controller('SubcategoriaDialogController', SubcategoriaDialogController);

    SubcategoriaDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'DataUtils', 'entity', 'Subcategoria', 'Producto', 'Categoria'];

    function SubcategoriaDialogController ($timeout, $scope, $stateParams, $uibModalInstance, DataUtils, entity, Subcategoria, Producto, Categoria) {
        var vm = this;

        vm.subcategoria = entity;
        vm.clear = clear;
        vm.byteSize = DataUtils.byteSize;
        vm.openFile = DataUtils.openFile;
        vm.save = save;
        vm.productos = Producto.query();
        vm.categorias = Categoria.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.subcategoria.id !== null) {
                Subcategoria.update(vm.subcategoria, onSaveSuccess, onSaveError);
            } else {
                Subcategoria.save(vm.subcategoria, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ecofruitApp:subcategoriaUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
