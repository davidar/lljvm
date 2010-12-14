package lljvm.runtime;

import java.util.*;
import lljvm.util.ReflectionUtils;

public class Environment {
    public Memory memory;
    public Error error;
    public IO io;
    public Function function;
    
    private Map<String,CustomLibrary> libraries;
    
    public Environment( ) {
        libraries = new HashMap<String,CustomLibrary>( );
        
        memory = new Memory( );
        error = new Error( );
        loadCustomLibrary( memory );
        loadCustomLibrary( error );
        
        
        io = new IO( );
        loadCustomLibrary( io );
        
        function = new Function( );
        loadCustomLibrary( function );
    }
    
    public void loadCustomLibrary( CustomLibrary library ) {
        Class<?> libClass = library.getClass( );
        String libraryName = libClass.getCanonicalName( );
        
        if ( !libraries.containsKey( libraryName ) ) {
            library.initialiseEnvironment( this );

            //java.lang.System.err.println( "Loaded CustomLibrary: " + libraryName );

            libraries.put( libraryName, library );
        }
    }
    
    public CustomLibrary getInstanceByName( String name ) {
        String dottedName = name.replace("/",".");
        CustomLibrary lib = libraries.get( dottedName );
        
        if ( lib == null ) {
            // class not yet loaded, attempt to load it...
            try {
                Class<?> c = ReflectionUtils.getClass( dottedName );
                loadCustomLibrary( (CustomLibrary)c.newInstance( ) );
            } catch (ClassNotFoundException e) {
                e.printStackTrace( );
            } catch (InstantiationException e) {
                e.printStackTrace( );
            } catch (IllegalAccessException e) {
                e.printStackTrace( );
            }
            
            lib = libraries.get( dottedName );
        }
        
        //java.lang.System.err.println( "getInstanceByName: " + dottedName + " --> " + lib );
        return lib;
    }
    
    public CustomLibrary getInstanceForClass( Class<?> cls ) {
        String libraryName = cls.getCanonicalName( );
        return getInstanceByName( libraryName );
    }
}
